为了精确定位耗时函数会插桩统计所有的函数耗时,耗时函数被存放到一个long类型的数组中(7.6m)
- 含有PUT/READ FIELD指令的函数 或者 一些return i++这样简单的函数 不插桩 ；对于频繁调用的函数要配置黑名单来降低整个方案对性能的损耗
- 避免方法数暴增，给每个函数分配一个id
- 每个方法都调用System.nanoTime对性能有损耗，5ms的函数可以忽略，通过定时5ms刷新一个时间变量，然后每个方法直接读取更新过的时间变量
- 由于数据庞大，需要对数据进行整合与裁剪(过滤掉耗时5ms的函数，将容量控制在30)，并分析出一个能代表卡顿堆栈的key（遍历buffer，计算出一个调用树及每个函数的执行耗时，并对每一级中的一些相同执行函数做聚合，分析出主要耗时(耗时大于30%的函数用id组成key)的那一级，作为代表卡顿堆栈的key）


# See ./record_android_trace --help for more
./record_android_trace -o trace_file.perfetto-trace -t 30s -b 64mb \
sched freq idle am wm gfx view binder_driver hal dalvik camera input res memory

./record_android_trace -c config.pbtx -o trace_file.perfetto-trace

adb shell perfetto -o /data/misc/perfetto-traces/trace_file.perfetto-trace -t 20s \
sched freq idle am wm gfx view binder_driver hal dalvik camera input res memory

系统跟踪 app
adb pull /data/local/traces .
