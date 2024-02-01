import subprocess, os

PACKAGE_NAME = 'com.electrolytej.vi'
FILE_NAME = PACKAGE_NAME+".hprof"
FILE_PATH = "/data/local/tmp/" + FILE_NAME
# adb pull  ${FILE_PATH} .
# hprof-conv -z ${FILE_NAME} "mat-${FILE_PATH}"
subprocess.run('adb shell pkill -l 10 ' + PACKAGE_NAME,shell=True,stdout=subprocess.PIPE)
subprocess.run("adb shell am dumpheap %s %s" % (PACKAGE_NAME, FILE_PATH),shell=True,stdout=subprocess.PIPE)
subprocess.run('adb pull %s %s' % (FILE_PATH,os.getcwd()),shell=True,stdout=subprocess.PIPE)
print('adb pull %s %s' % (FILE_PATH,os.getcwd()))
os.popen('hprof-conv -z %s %s' % (FILE_NAME,'mat-'+FILE_NAME)).read()
a = os.popen('adb shell dumpsys meminfo %s' % PACKAGE_NAME).read()
with open('meminfo.txt','w') as f:
    f.write(a)