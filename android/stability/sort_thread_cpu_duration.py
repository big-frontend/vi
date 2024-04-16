import sys,os,dataclasses
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
    
    with open(sys.argv[1],"r",encoding = 'utf-8') as f:
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
        print('anr log','0'*100)
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
            #线程信息                and "Waiting" not in line and "TimedWaiting" not in line
            if "prio=" in line and "tid=" in line :
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
        print('thread:>>>>>>>>>>>>')        
        print(*l,sep='\n')
    print('event log','0'*100)
    with open(sys.argv[2],"r",encoding = 'utf-8') as f:
        while True:
            line = f.readline()
            if not line: break
            if (appPkg in line and  'activity_launch_time' in line) or 'am_anr' in line:
                print(line.strip(),sep='\n')
    print('main log','0'*100)            
    with open(sys.argv[3],"r",encoding = 'utf-8') as f:
        while True:
            line = f.readline()
            if not line: break
            if 'lowmemorykiller' in line or 'Background young' in line or 'IPCThreadState' in line:
                print(line.strip(),sep='\n')

if __name__=="__main__":
    main()
