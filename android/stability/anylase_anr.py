import sys,os,dataclasses
from datetime import datetime,timedelta
def convertTs(ts):
    if ts >= 1000:
        return f"{ts/1000}s"
    else:
        return f"{ts}ms"
class ThreadInfo:
    t:str
    utm:int
    stm:int
    cpu_duration:int
    def __str__(self) -> str:
        return f"cpu_duration:{convertTs(self.cpu_duration)} utm/stm:{convertTs(self.utm)}/{convertTs(self.stm)} thread:{self.t}"

from collections import OrderedDict
import collections

def main():
    l = []
    anr_log = ''
    event_log = ''
    main_log = ''
    sys_log = ''
    for root, ds, fs in os.walk(sys.argv[1]):
        for f in fs:       

            if 'anr' in f:             
                anr_log =  f"{root}\\{f}"
            elif 'event' in f:
                event_log =  f"{root}\\{f}"
            elif 'main.txt' == f:
                main_log = f"{root}\\{f}"
            elif 'system.txt' == f:
                sys_log = f"{root}\\{f}"
            elif 'system_main_crash.txt' == f:
                main_log = sys_log = f"{root}\\{f}"
        
    
    with open(anr_log,"r",encoding = 'utf-8') as f:
        appMainPid = ''
        appPkg = ''
        while True:
            line = f.readline()
            if "CPU usage" in line:
                break
            
            if 'PID' in line:
                appMainPid = line.split("PID:")[1].strip()
            if 'Package' in line:
                appPkg = line.split("Package:")[1].split()[0].strip()
                
        #cpu usage
        print('0'*100,'anr log','0'*100)
        if "CPU usage" in line:
            print(line.strip(),sep='\n')
        while True:
            line = f.readline()
            if 'TOTAL:' in line:
                print(line.strip(),sep='\n')
                break
            percent = line.split()[0].split("%")[0]
            
            if float(percent) > 2.0:
               print('\t'+line.strip(),end='\n')
            
            #if percent > '1.0%' and  ('kswapd0' in line or 'kworker' in line or 'pid' in line):
            #    print(line.strip(),sep='\n')     
        while True:
            line = f.readline()
            if not line:
                break
                
            if "----- pid" in line:
                if l:
                    # sorted(l,key=lambda t: t.cpu_duration)
                    l = sorted(l,key=lambda t: t.cpu_duration,reverse=True)
                    l = filter(lambda t: t.cpu_duration !=0, l)
                    print('thread:')
                    print(*l,sep='\r\n')
                    
                    l = []
                print(line.strip(),end='\n')
                continue
            #进程内存信息    
            if "Max memory" in line or "Total memory" in line or "Heap:" in line:
                print(line.strip(),sep='\n')
                continue
            #线程信息               
            if "prio=" in line and "tid=" in line  and "Waiting" not in line and "TimedWaiting" not in line:
                t = ThreadInfo()
                t.t=line.strip()
                while True:
                    line = f.readline()
                    if not line:
                        break
                    if line in ['\n','\r\n']:
                        break
                    if "utm=" in line:
                        utm = 0
                        for e in line.split(" "):
                            if "utm" in e:
                                utm = int(e.split("=")[1])
                                t.utm = utm * 10
                                break
                    if "stm=" in line:
                        stm = 0
                        for e in line.split(" "):
                            if "stm" in e:
                                stm = int(e.split("=")[1])
                                t.stm = stm * 10
                                break
                t.cpu_duration = (t.utm + t.stm) 
                l.append(t)
    
    if l:
        # sorted(l,key=lambda t: t.cpu_duration)
        l = sorted(l,key=lambda t: t.cpu_duration,reverse=True)
        l = filter(lambda t: t.cpu_duration !=0, l)
        print('thread:')        
        print(*l,sep='\n')
    print('0'*100,'event log','0'*100)
    happen_anr_time = ''
    with open(event_log,"r",encoding = 'utf-8') as f:
        while True:
            line = f.readline()
            if not line: break
            if (appPkg in line and  'activity_launch_time' in line) or  ('binder_sample' in line and appPkg in line) or 'dvm_lock_sample' in line:
                print(line.strip(),sep='\n')
            if 'am_anr' in line:
                print(line.strip(),sep='\n')
                happen_anr_time = line.split()[1]
            
    print('0'*100,'main log','0'*100)
    a = False
    b = False
    happen_anr_time = datetime.strptime(happen_anr_time, '%H:%M:%S.%f')
    happen_anr_time_5_before = happen_anr_time - timedelta(hours=0, minutes=0,seconds =5)
    d = False
    with open(main_log,"r",encoding = 'utf-8') as f:
        while True:
            line = f.readline()
            if not line: break
            if not d and 'beginning of main' not in line:
                continue
            elif 'beginning of main' in line:
                d = True
            if 'beginning' in line:continue
            
            cur_data_time = datetime.strptime(line.split()[1],'%H:%M:%S.%f')
            if not a and cur_data_time > happen_anr_time_5_before:
                print('\n')
                print(appMainPid,appPkg,happen_anr_time_5_before,"anr事件",">"*70,end='\r\n')                
                a = True
            if not b and cur_data_time > happen_anr_time:                    
                print(appMainPid,appPkg,happen_anr_time,"anr事件","<"*70,end='\r\n')
                print('\n')
                b = True
            #if appMainPid in line and ('IPCThreadState' in line or 'ANR_LOG ' in line):
                #print(line.strip(),sep='\n')
            elif 'lowmemorykiller' in line or 'Background young' in line or 'Slow operation' in line or 'Slow delivery' in line or 'InputDispatcher' in line or 'InputReader' in line or appMainPid in line:
                print(line.strip(),sep='\n')
    print('0'*100,'system log','0'*100) 
    with open(sys_log,"r",encoding = 'utf-8') as f:
        while True:
            line = f.readline()
            if not line: break
            if 'Slow operation' in line or 'Slow delivery' in line:
                print(line.strip(),sep='\n')
            

if __name__=="__main__":
    main()