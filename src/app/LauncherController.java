package app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class LauncherController implements Initializable {

    private final String[] data_packs = {"forge-1.12.2-14.23.5.2854-installer", "spongeforge-1.12.2-2838-7.2.0", "extrautils2-1.12-1.9.9", "Pixelmon-1.12.2-8.1.2-universal"};
    private final String[] urls = {
            "https://uc20d673fbd5e74d0bb4fbabd0d9.dl.dropboxusercontent.com/cd/0/get/BGRYBPL2pJSOLEURnsfTaSw3II-nCAfaYC5OEMPq1dsrcNdmm5juP9jGpNSQ3eJu_SN2fBJkslZQHFmtKj32x9S9O8Yx8uVDTaac5iVPi12ZGWjdYPmfNmxQQqq_AluIH80/file#",
            "https://uc10d8a02cc8d4cc7184b22aafd1.dl.dropboxusercontent.com/cd/0/get/BGTjSveorg9_sVucy2Y0pDLv4JzcfLz3TtdRrOLn9aMhWUYA7X2GO5t9XhMz7TIJAOwFId0pDv4GhzZ9G5y1uP-PC9M0CU5KDgHSe9gB3_PAISkxYQRYN4_4Gbm1Suky3xI/file#",
            "https://uc9f4b126bc46cee9872e319def4.dl.dropboxusercontent.com/cd/0/get/BGTbVpUAidCLcXHBJ6KweRKRFMqYZe_tysOQhDCVZHN3fdWj4Cd83gII2FkvNjn7swk_kmqb77mZJ4QKz2Thi56rP5BL0XU6nxjcp2mWS-rNQj_J3CzqfbJYWP1cyWR6RF4/file#",
            "https://uccd5593825829fc0ddd0a63ed46.dl.dropboxusercontent.com/cd/0/get/BGT0-S1g5tPpn-R7oqGmRKIJCZwAoS7Ut1zafepVHbgtU3LVTwYz5kjdLCoHe3ThJz3Pi3l3z-gaofyxIBequWSNh50xOQMM6YFuueTxqdh0uh148duQ8vIJtSUjfb7Z69g/file#"
    };
    private final String destPath = "." + File.separator + "data" + File.separator;
    private final String logPath = "." + File.separator + data_packs[0] + ".jar.log";

    private final String launcherChecker = "launcher_profiles.json";
    private final String cache = "launcherPath.txt";

    private final String correctImgURI = String.valueOf(getClass().getResource("/icon/check.png"));
    private final String incorrectImgURI = String.valueOf(getClass().getResource("/icon/delete.png"));
    private final String spinnerImgURI = String.valueOf(getClass().getResource("/icon/Spinner.gif"));

    private List<String> requirements = new ArrayList<>();
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private File selectedDirectory;

    @FXML
    private AnchorPane ap;

    @FXML
    private ImageView forgeIcon,spongeIcon,extraIcon,pixelIcon;
    private ImageView[] icons;

    @FXML
    private Label alertDownload, alertDir, alertInstall;

    @FXML
    private TextField dirTextField;

    @FXML
    private Button installBtn, downloadBtn0, downloadBtn1, downloadBtn2, downloadBtn3;
    private Button[] buttons;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            alertDownload.setVisible(false);
            alertDir.setVisible(false);
            alertInstall.setVisible(false);

            System.out.println("Hello Launcher");
            icons = new ImageView[]{forgeIcon, spongeIcon, extraIcon, pixelIcon};
            buttons = new Button[]{downloadBtn0, downloadBtn1, downloadBtn2, downloadBtn3};
            File destDir = new File(destPath);
            if (!destDir.exists()) {
                boolean result = destDir.mkdir();
            }

            dataPacksChecker();
            requirementChecker();
        });
    }

    private void dataPacksChecker() {
        Image iconImage;

        for (int i=0; i<data_packs.length; i++) {
            File file = new File(destPath + data_packs[i] + ".jar");
            iconImage = new Image(incorrectImgURI);
            if (file.exists()) {
                iconImage = new Image(correctImgURI);
                requirements.add(data_packs[i]);
                buttons[i].setDisable(true);
            } else {
                alertDownload.setVisible(true);
            }
            icons[i].setImage(iconImage);
        }

        File launcherPathFile = new File(destPath + cache);
        if (launcherPathFile.exists()) {
            try {
                Scanner myReader = new Scanner(launcherPathFile);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                    File file = new File(data);
                    if (file.exists()) {
                        directoryChooser.setInitialDirectory(file);
                        selectedDirectory = file;
                        dirTextField.setText(data);
                        requirements.add(launcherChecker);
                    }
                }
                myReader.close();
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }

    private void requirementChecker() {
        if (requirements.size() >= 5) {
            installBtn.setDisable(false);
            alertInstall.setVisible(true);
        } else {
            installBtn.setDisable(true);
            alertInstall.setVisible(false);
        }
        System.out.println("requirement :" + Arrays.toString(requirements.toArray()));
    }

    public void downloadBtnOnClick(ActionEvent event) {
        Button button = (Button) event.getSource();
        button.setDisable(true);
        int index = Integer.parseInt(button.getId().split("Btn")[1]);
        String target = data_packs[index];

        System.out.println("Downloading... " + target);
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        //Background work
                        final CountDownLatch latch = new CountDownLatch(1);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //FX Stuff done here
                                    alertDownload.setText("Downloading... " + target);
                                    alertDownload.setTextFill(Color.BLACK);
                                    alertDownload.setVisible(true);

                                    icons[index].setImage(new Image(spinnerImgURI));
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });
                        latch.await();
                        //Keep with the background work
                        try {
                            downloadUsingNIO(urls[index], destPath + target + ".jar");
                            File targetFile = new File(destPath + target + ".jar");

                            KeyFrame update = new KeyFrame(Duration.seconds(0.5), event -> {
                                // update label here. You don't need to use Platform.runLater(...), because Timeline makes sure it will be called on the UI thread.
                                changeAlertDownload(targetFile.exists(), index, target);
                                requirementChecker();
                            });
                            Timeline tl = new Timeline(update);
                            tl.play();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                };
            }
        };
        service.start();
    }

    private void changeAlertDownload(boolean exist, int index, String target) {
        if (exist) {
            icons[index].setImage(new Image(correctImgURI));
            System.out.println("Download Successful: " + target);
            alertDownload.setText("Download Successful: " + target);
            alertDownload.setTextFill(Color.GREEN);

            if (!requirements.contains(target)) {
                requirements.add(target);
            }
        } else {
            icons[index].setImage(new Image(incorrectImgURI));
            System.out.println("Download Failed: " + target);
            alertDownload.setText("Download Failed: " + target);
            alertDownload.setTextFill(Color.RED);
        }
    }


    public void dirChooserBtnOnClick(ActionEvent event) {
        selectedDirectory = directoryChooser.showDialog(ap.getScene().getWindow());
        if (selectedDirectory == null) {
            return;
        }
        dirTextField.setText(selectedDirectory.getAbsolutePath());
        boolean launcherCheck = false;
        for (File f : selectedDirectory.listFiles()) {
            if (f.getName().equals(launcherChecker)) {
                launcherCheck = true;

                alertDir.setVisible(false);
                if (!requirements.contains(launcherChecker)) {
                    requirements.add(launcherChecker);
                }
            }
        }
        directoryChooser.setInitialDirectory(selectedDirectory);
        if (!launcherCheck) {
            alertDir.setVisible(true);
            requirements.remove(launcherChecker);
        } else {
            File cacheFile = new File(destPath + cache);
            if (!cacheFile.exists()) {
                try {
                    boolean result = cacheFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                FileWriter fileWriter = new FileWriter(cacheFile.getAbsoluteFile());
                fileWriter.write(selectedDirectory.getAbsolutePath());
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        requirementChecker();
    }

    public void installBtnOnClick(ActionEvent event) {
        try {
            Runtime runTime = Runtime.getRuntime();
            String executablePath = destPath + data_packs[0];
            Process process = runTime.exec("java -jar " + executablePath + ".jar");
            process.waitFor();

            System.out.println("exit value: " + process.exitValue());
            if (process.exitValue() == 0) {
                File forgeVersion = new File(selectedDirectory + File.separator + "1.12.2-forge-14.23.5.2854");
                if (!forgeVersion.exists()) {
                    alertInstall.setText("Install Client Failed.");
                    alertInstall.setTextFill(Color.RED);
                }

                File modsDir = new File(selectedDirectory + File.separator +"mods");
                if (!modsDir.exists()) {
                    boolean result = modsDir.mkdir();
                }

                alertInstall.setText("Installing mods...");

                for (File f : new File(destPath).listFiles()) {
                    String filename = f.getName();
                    if (!filename.equals("launcherPath.txt") && !filename.startsWith("forge") && requirements.contains(filename.split(".jar")[0])) {
                        File destFile = new File(modsDir + File.separator + f.getName());
                        Files.copy(f.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("COPY TO " + destFile.getAbsolutePath());
                    }
                }

                alertInstall.setText("Installation Successful");
                alertInstall.setTextFill(Color.GREEN);

                File log = new File(logPath);
                if (log.exists()) {
                    log.delete();
                }

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void downloadUsingNIO(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
