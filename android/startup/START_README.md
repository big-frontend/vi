 * App启动分为冷启动、热启动
 * 冷启动：
 *  - 基础组件 amp bundle av image storage network 必须放在主线程初始
 *  - 其他组件 map 可以异步初始化
 * 热启动：
 *
 * App#attachBaseContext --> ContentProvider#attachInfo --> ContentProvider#onCreate--->App#onCreate
 *
 * Android团队提供的startup库，主要是在ContentProvider#onContext初始化各个Initializer
 * ps: 关于startup库实现的吐槽点，用ContentProvider去做初始化，感觉没啥意义，多了一次跨进程创建ContentProvider(空ContentProvider耗时2ms)，
 * 对于追求极致的启动速度，不应该减少这个操作嘛。为什么就不放在Application这个类中做，我觉得可以把InitializationProvider的代码整合到Application中。
 *
 * 总之一句话startup库就是垃圾，应该解决的是各个启动项谁可以同步初始化，谁可以异步初始化。fast start up.
 *
 *
 * matrix：
 *  * firstMethod.i       LAUNCH_ACTIVITY   onWindowFocusChange   LAUNCH_ACTIVITY    onWindowFocusChange
 *  * ^                         ^                   ^                     ^                  ^
 *  * |                         |                   |                     |                  |
 *  * |---------app---------|---|---firstActivity---|---------...---------|---careActivity---|
 *  * |<--applicationCost-->|
 *  * |<----firstScreenCost---->|
 *  * |<---------------------------allCost(cold)------------------------->|
 *  * .                         |<--allCost(warm)-->|
 *
 *  优化点：异步任务可以在splash页面onWindowFocusChange也就是出现窗口时候await，等任务都执行完成，才让其进入正在的main页面
 *
 *
 *  关于启动优化：
 *  - 闪屏优化：高端机(Android 6.0 或者 Android 7.0)去掉预览窗口 ； 合并splash与main页面，少一个activity节省100ms左右，但是会增加管理业务的复杂度
 *  - 业务梳理：(注意懒加载集中化，容易造成首页出现后无法交互)
 *  1. 根据业务的优先级加载,哪些是必须加载，哪些是可以懒加载
 *  2. 根据业务场景，通过扫一扫启动，只加载几个模块
 *  3. 推动产品做功能取舍
 *  - 业务优化
 *  1. 最理想的方式通过算法优化，比如加解密 1s被优化为10ms
 *  2. 异步预加载，要注意过多的线程预加载会让业务逻辑变得复杂
 *  - 线程优化(主要减少cpu的调度带来的波动，让启动更稳定)
 *  1. 需要控制线程的数量，线程太多会互相竞争cpu资源，可以采用线程池统一管理线程，并且根据机器性能来控制数量(cpu核心数，io密集型2*core+1,cpu密集新core+1)
 *  2. 检查线程间的锁，避免主线程长时间等待其他线程释放锁
 *  3. 使用优秀的启动库阿里alpha 微信mmkernel
 *  - GC 优化
 *  1. 减少gc次数，避免主线程长时间卡顿。通过Debug.startAllocCounting监控gc的耗时情况，特别是阻塞式同步 GC 的总次数和耗时。
 *  如果存在gc同步等待，就需要用Allocation工具分析内存，有可能存在这样的情况：使用大量的字符串拼接创建大量对象，特别是序列化与反序列化过程；
 *  频繁创建对象，比如网络路与图片库中的Byte数组、Buffer数组可以复用，可以考虑移到native
 *  2. java对象的逃逸(一个对象的指针被多个方法或者线程引用)，保证一个对象生命周期尽量短，在栈上就被销毁
 *  - 系统调用优化(通过systrace查看System Server cpu的使用情况)
 *  1. 尽量不做系统调用pms操作、binder调用等待
 *  2. 不要过早启动其他进程，回合System Server相互竞争cpu资源，当系统资源不足时，就会触发low memory killer，导致系统杀死或者拉起(保活)大量进程，从而影响前台进程
 *
 *
 *  进阶：
 *  - io优化：使用随机读写的数据结构
 *  - 数据重排
 *  - 类重排:使用redex重排dex中的类顺序
 *  - 资源文件重排：修改7zip支持传入列表顺序
 *  - 类加载：通过hook java虚拟机，去掉verify class的过程，在类加载的过程可以提升50%的速度
 *  - 保活：Hyper Boost or Hardcoder
 *  - 插件化与热修复：不行了

 App / Activity 启动
 - App启动通过Zygote fork出子进程，会连通art虚拟机已经虚拟机预加载的资源和类一块复制到子进程(预热)，然后在启动ActivityThread#main,完成Application与ContentProvider的生命周期之后，Looper还会等待来自fwk 关于Launch Activity的事件。
 - 当启动Activity时，fwk会将目标Activity所在的任务栈移动到前台，然后发送启动事件给目标App进程，当目标Activity完成create、start、resume生命周期后以及View树的测绘处于resumed就可见了