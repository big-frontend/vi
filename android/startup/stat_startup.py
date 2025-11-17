import sys,os,dataclasses,subprocess
from datetime import datetime,timedelta

PACKAGE_NAME = 'com.lingan.seeyou'
LAUNCHER_ACTIVITY = 'com.lingan.seeyou/.ui.activity.main.SeeyouActivity'
def run(cmd_list):
    completedProcess = subprocess.run(
        cmd_list, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    returncode = completedProcess.returncode
    if returncode == 0:
        ret = completedProcess.stdout.decode('utf-8').strip()
        return ret.split('\n') if ret else ''
    else:
        # sys.stdout.write()
        # log.error(completedProcess.stderr.decode('utf-8').strip())
        return ''

def main():
    total_time = 0
    count = 10
    for i in range(count):
        cmd = 'adb shell am force-stop %s' % PACKAGE_NAME
        run(cmd)
        #清理缓存
#         cmd = 'adb shell pm clear %s' % PACKAGE_NAME
#         run(cmd)
        ret = run(f"adb shell am start -W {LAUNCHER_ACTIVITY}")
        for line in ret:
            if 'TotalTime' in line:
                try:
                    t = line.split(':')[1].strip()
                    total_time += int(t)
                    print(f"第{i}次启动耗时 {total_time}ms\t")
                except:
                    print('error line:',line)
                break
    print(f"{count}次，平均启动耗时 {total_time/count}ms")


if __name__=="__main__":
    main()