package deployment;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.Random;
import java.util.jar.Manifest;

import ccre.deployment.Artifact;
import ccre.deployment.DepJar;
import ccre.deployment.DepJava;
import ccre.deployment.DepProject;
import ccre.deployment.DepRoboRIO;
import ccre.deployment.JarBuilder;
import ccre.deployment.Shell;
import ccre.deployment.DepRoboRIO.RIOShell;
import ccre.deployment.eggs.EggHatcher;
import ccre.frc.FRCApplication;
import ccre.log.Logger;

public class DepKangaroo {
    private static final String DEFAULT_USERNAME = "robotics";
    private static final String DEFAULT_PASSWORD = "robotics1540";
    private static final String JAR_LOCATION = "/home/robotics/Kangaroo.jar";
    
    private static final Random random = new Random();

    public static DepKangaroo connect(String address) throws UnknownHostException {
        InetAddress inaddr;
        try {
            inaddr = InetAddress.getByName(address);
            try (Socket sock = new Socket()) {
                sock.connect(new InetSocketAddress(inaddr, 22), 500);
            }
        } catch (IOException e) {
            throw new UnknownHostException("Cannot connect to Kangaroo!");
        }

        return new DepKangaroo(inaddr);
    }

    public static KangarooShell connectAndVerify(String address) throws IOException {
        DepKangaroo kangaroo = connect(address);
        return kangaroo.openShell();
    }

    private final InetAddress ip;

    private DepKangaroo(InetAddress ip) {
        this.ip = ip;
    }

    public KangarooShell openShell() throws IOException {
        return new KangarooShell(ip, DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    public class KangarooShell extends Shell {
        private KangarooShell(InetAddress ip, String username, String password) throws IOException {
            super(ip, username, password);
        }

        public void archiveLogsTo(File destdir) throws IOException {
            if (this.exec("ls ccre-storage/log-* >/dev/null 2>/dev/null") == 0) {
                long name = random.nextLong();
                this.execCheck("tar -czf logs-" + name + ".tgz ccre-storage/log-*");
                this.execCheck("mkdir /tmp/logs-" + name + "/ && mv ccre-storage/log-* /tmp/logs-" + name + "/");
                Files.copy(this.receiveFile("logs-" + name + ".tgz"), new File(destdir, "logs-" + name + ".tgz").toPath());
                this.execCheck("rm logs-" + name + ".tgz");
            }
        }

        public void downloadCode(File jar) throws IOException {
            Logger.info("Starting deployment...");
            sendFileTo(jar, JAR_LOCATION);
            Logger.info("Download complete.");
        }

        public void stopRobot() throws IOException {
            exec("killall -9 java");
        }

        public void startRobot() throws IOException {
            execCheck("java -jar " + JAR_LOCATION);
        }

        public void downloadAndStart(File code) throws IOException {
            stopRobot();
            downloadCode(code);
            startRobot();
        }

        public void downloadAndStart(Artifact result) throws IOException {
            downloadAndStart(result.toJar(false).toFile());
        }
    }

    public static Artifact build(Class<?> main) throws IOException {
        Artifact viewko = DepJava.build(DepProject.directory("../Viewko/src"), new File(DepProject.ccreProject("CommonChickenRuntimeEngine"), "CCRE.jar"));
        Artifact output = DepJava.build(DepProject.directory("src"), DepRoboRIO.getJarFile(DepRoboRIO.LIBS_THICK), viewko.toJar(false).toFile());
        Manifest manifest = DepJar.manifest("Main-Class", main.getName());
        return DepJar.combine(manifest, JarBuilder.DELETE, output, DepRoboRIO.getJar(DepRoboRIO.LIBS_THIN), viewko);
    }
}
