package com.jamesfchen.viapm.counter

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
//import javax.mail.Session
//import javax.mail.Transport
//import javax.mail.internet.MimeMessage
//import javax.mail.internet.InternetAddress
import javax.inject.Inject

class FileLinesCounter extends DefaultTask {
    public List<File> srcDirs
    @Input
    int l_totals = 0
    @Input
    int lines = 0
    @Input
    int j_l_totals = 0
    @Input
    int j_lines = 0
    @Input
    int j_f_totals = 0
    @Input
    int j_files = 0
    @Input
    int k_l_totals = 0
    @Input
    int k_lines = 0
    @Input
    int k_f_totals = 0
    @Input
    int k_files = 0
    @Input
    int c_l_totals = 0
    @Input
    int c_lines = 0
    @Input
    int c_f_totals = 0
    @Input
    int c_files = 0

    @Inject
    FileLinesCounter(List<File> srcDirs) {
        this.srcDirs = srcDirs
    }

    @TaskAction
    def startCounting() {
        if (srcDirs == null || srcDirs.size() == 0) return
        srcDirs.each { srcDir ->
            printDirectoryTree(srcDir)
        }
        println("CounterPlugin task>>> totals : ${j_f_totals} java/${j_l_totals} lines, " +
                "${k_f_totals} kotlin/${k_l_totals} lines," +
                " ${c_f_totals} c or c++/${c_l_totals} lines  \n\r")
        def map = new HashMap<String, String>()
        map.put("|  java|", "|  java|${j_f_totals} |${j_l_totals}|")
        map.put("|  kotlin|", "|  kotlin|${k_f_totals}|${k_l_totals}|")
        map.put("|  c or c++|", "|  c or c++|${c_f_totals}|${c_l_totals}|")
        File file = new File(project.rootDir, "README.md")
        replace(file, map)
        def ext = project.extensions['counterConfig'] as CounterExtension
        StringBuilder content = new StringBuilder()
                .append("""
                         <html>
                           <head></head>
                           <body>
                            <p>size:${ ext.pickupModules ==null ?'all':ext.pickupModules.size() } modules${ext.pickupModules} </p>
                              <table align="center" border="1" cellpadding="0" cellspacing="0" width="600" style="border-collapse: collapse;">
                            　<tr>
                            　　<td> language </td>
                                <td> files </td>
                                <td> lines </td>
                            　</tr>
                            　<tr>
                            　　<td> java </td>
                            　　<td> ${j_f_totals} </td>
                            　　<td> ${j_l_totals} </td>
                            　</tr>
                            
                            　<tr>
                            　　<td> kotlin </td>
                            　　<td> ${k_f_totals} </td>
                            　　<td> ${k_l_totals} </td>
                            　</tr>
                            <tr>
                            　　<td> c or c++ </td>
                               <td>${c_f_totals}</td>
                               <td>${c_l_totals}</td>
                            　</tr>
                            </table>
                         <p> 新增文件</p>
                              <table align="center" border="1" cellpadding="0" cellspacing="0" width="600" style="border-collapse: collapse;">
                                　<tr>
                                　　<td> language </td>
                                    <td> files </td>
                                    <td> lines </td>
                                　</tr>
                                　<tr>
                                　　<td> java </td>
                                　　<td> ${j_f_totals} </td>
                                　　<td> ${j_l_totals} </td>
                                　</tr>
                                
                                　<tr>
                                　　<td> kotlin </td>
                                　　<td> ${k_f_totals} </td>
                                　　<td> ${k_l_totals} </td>
                                　</tr>
                                <tr>
                                　　<td> c or c++ </td>
                                   <td>${c_f_totals}</td>
                                   <td>${c_l_totals}</td>
                                　</tr>
                            </table>
                           </body>
                         </html>
                """)
//        sendEmail(content)
//        project.exec {
//            println "[post email] post  start"
//            executable isWindows() ? "python" : "python3"
//            workingDir project.rootDir
//            def argv = []
//            argv << "${project.rootDir}${File.separator}script${File.separator}post_email.py"
//            argv << content.toString()
//            args = argv
//            println "[post email] post  end"
//        }
    }
//      需要导入classpath 'javax.mail:javax.mail-api:1.5.1'
//    void sendEmail(String content) {
//        def smtpserver = 'smtp.163.com'
//        def username = 'hawks93jf@163.com'
//        def password = 'YUAGSGWLPUZFHAEC'
//        def sender = 'hawks93jf@163.com'
//        def receiver = ['hawksjamesf@gmail.com']
//        Properties p = new Properties()
//        p.setProperty("mail.transport.protocol", 'smtp')
//        p.setProperty("mail.smtp.host", smtpserver)
//        p.setProperty("mail.smtp.auh", true)
//
//        Session session = Session.getInstance(p)
//        session.setDebug(true)
//        MimeMessage message = new MimeMessage(session)
//        message.setFrom("hawks93jf <hawks93jf@163.com>")
//        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiver, "jamesfchen", "UTF-8"))
//        message.setSubject("文件统计监控", "UTF-8")
//        message.setContent(content, "text/html;charset=UTF-8")
//        message.setSentDate(new Date())
//        message.saveChanges()
//        Transport t = session.getTransport()
//        t.connect(username, password)
//        t.sendMessage(message, message.getAllRecipients())
//        t.close()
//    }

    def replace(File file, HashMap<String, String> map) {
        def keys = map.keySet()
        BufferedReader br = new BufferedReader(new FileReader(file));
        CharArrayWriter tempStream = new CharArrayWriter();
        String line = null
        while ((line = br.readLine()) != null) {
            for (int i = 0; i < keys.size(); i++) {
                def k = keys[i]
                if (line.contains(k)) {
                    line = line.replace(line, map.get(k))
                    print(k + " " + line + "\n")
                    break
                }
            }
            tempStream.write(line)
            tempStream.append(System.getProperty("line.separator"));
        }
        br.close()
        FileWriter out = new FileWriter(file)
        tempStream.writeTo(out)
        out.close()

    }

    def printDirectoryTree(File dir) {
        if (dir == null || !dir.isDirectory() || !dir.exists()) return
        int indent = 0
        StringBuilder sb = new StringBuilder()
        printDirectoryTree(dir, indent, sb);
        println("${dir.path}\n$sb>>> ${j_files} java/${j_lines} lines, " +
                "${k_files} kotlin/${k_lines} lines," +
                "${c_files} kotlin/${c_lines} lines," +
                "\n\r")
        this.lines = 0
        this.j_lines = 0
        this.k_lines = 0
        this.j_files = 0
        this.k_files = 0
        this.c_lines = 0
        this.c_files = 0

    }

    void printDirectoryTree(File dir, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(dir.getName());
        sb.append("/");
        sb.append("\n")
        dir.eachFile { theFile ->
            if (!theFile.name.contains('.DS_Store')) {
                if (theFile.isFile()) {
                    printFile(theFile, indent + 1, sb)
                } else if (theFile.isDirectory()) {
                    printDirectoryTree(theFile, indent + 1, sb)
                }
            }
        }

    }

    void printFile(File file, int indent, StringBuilder sb) {
        int lines = file.readLines().size()
        this.lines += lines
        this.l_totals += lines
        if (file.name.endsWith("kt")) {
            this.k_lines += lines
            this.k_l_totals += lines
            this.k_files += 1
            this.k_f_totals += 1
        } else if (file.name.endsWith("java")) {
            this.j_lines += lines
            this.j_l_totals += lines
            this.j_files += 1
            this.j_f_totals += +1
        } else if (file.name.endsWith("c") || file.name.endsWith("cpp") || file.name.endsWith("h")) {
            this.c_lines += lines
            this.c_l_totals += lines
            this.c_files += 1
            this.c_f_totals += +1
        }
        sb.append(getIndentString(indent))
                .append("+--")
                .append(file.getName())
                .append("(lines:")
                .append(lines)
                .append(")")
                .append("\n")
    }

    private String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }
}