import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.Set;
import java.util.Optional;
public class Controller {
 

    private Path pic_as_path = Paths.get("/Applications/microchip/xc8/v2.40/pic-as/bin/pic-as");
    public Core pic =null;
    private boolean flag = false;

    @FXML
    private TableView<SFR> table, table2;

    @FXML
    private TableColumn<SFR, String> addr, val, addr2, val2;

    @FXML
    private TextArea editor;

    private void raiseError(String err) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setContentText(err);
        alert.show();
    }
    public class SFR {
        private String addr, val;

        public String getAddr() {
            return addr;
        }

        public String getVal() {
            return val;
        }

        public void setAddr(String s) {
            addr = s;
        }

        public void setVal(String s) {
            val = s;
        }

        public SFR(String addr, String val) {
            setAddr(addr);
            setVal(val);
        }
    }

    public ObservableList<SFR> sfr(Core pic) {
        ObservableList<SFR> data = FXCollections.observableArrayList();
        HashMap<String, String> map = pic.registers;
        map.forEach((addr, val) -> {
            if (Integer.parseInt(addr, 16) < 8) {
                if (val == null) val = "null";
                if (addr == "00") addr = "INDF";
                else if (addr == "01") addr = "TMR0";
                else if (addr == "02") addr = "PCL";
                else if (addr == "03") addr = "STATUS";
                else if (addr == "04") addr = "FSR";
                else if (addr == "05") addr = "OSCCAL";
                else if (addr == "06") addr = "GPIO";
                else if(addr == "07") addr = "CMCON0";
                data.add(new SFR(addr, val));
            }
        });
        data.add(new SFR("TRISGPIO", pic.getTrisgpio_Reg()));
        data.add(new SFR("OPTION", pic.getOption_Reg()));
        data.add(new SFR("W Register", pic.getW_Reg()));
        return data;
    }

    public ObservableList<SFR> reg(Core pic) {
        ObservableList<SFR> data = FXCollections.observableArrayList();
        HashMap<String, String> map = pic.registers;
        map.forEach((addr, val) -> {
            if (Integer.parseInt(addr, 16) >= 16) {
                if (val == null) val = "null";
                data.add(new SFR(addr, val));
            }
        });
        return data;
    }

    @FXML
    private void run() {
        if(pic == null) {
            raiseError("Compile before stepping or running.");
            return;
        }
        if(flag) {
            Set<Thread> s = Thread.getAllStackTraces().keySet();
            for (Thread i : s) {
                if(i.getName().equals("running_thread")) {
                    i.interrupt();
                    flag = false;
                }
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    int flag_in = pic.step();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            updateTables();
                        }
                    });
                    if(flag_in==1) return;
                    try {
                        Thread.sleep(0, 1);
                    }
                    catch(InterruptedException e){
                        return;
                    }
                }
            }
        }, "running_thread").start();
        flag = true;
    }

    @FXML
    private void step() {
        if(pic == null) {
            raiseError("Compile before stepping or running.");
            return;
        }
        if(flag) {
            Set<Thread> s = Thread.getAllStackTraces().keySet();
            for (Thread i : s) {
                if(i.getName().equals("running_thread")) {
                    i.interrupt();
                    flag = false;
                }
            }
        }
        else {
            pic.step();
            updateTables();
        }
    }

    @FXML
    private void reset() {
        if(flag) {
            Set<Thread> s = Thread.getAllStackTraces().keySet();
            for (Thread i : s) {
                if(i.getName().equals("running_thread")) {
                    i.interrupt();
                    flag = false;
                }
            }
        }
        init_core();
        updateTables();
    }

    @FXML
    private void stop() {
        if(flag) {
            Set<Thread> s = Thread.getAllStackTraces().keySet();
            for (Thread i : s) {
                if(i.getName().equals("running_thread")) {
                    i.interrupt();
                    flag = false;
                }
            }
        }
    }

    @FXML
    private void close() {Platform.exit();}

    @FXML
    private void delete() {
        if(flag) {
            Set<Thread> s = Thread.getAllStackTraces().keySet();
            for (Thread i : s) {
                if(i.getName().equals("running_thread")) {
                    i.interrupt();
                    flag = false;
                }
            }
        }
        editor.clear();
        pic = null;
        table.setItems(null);
        table2.setItems(null);
    }

    @FXML
    private void openSearch() {
        editor.clear();
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("ASM files (.asm)", ".*.asm", "*.as", "*.s"));
            File file = fc.showOpenDialog(table.getScene().getWindow());
            if (file != null) {
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    editor.appendText(scan.nextLine()+"\n");
                }
                scan.close();
            }
            else {
            }
            file = null;
            fc = null;
        }
        catch (Exception e) {
            raiseError("Error when trying to open file chooser.");
        }
    }


    @FXML
    private void getPath() {
        TextInputDialog dialog = new TextInputDialog(pic_as_path.toString());
        dialog.setTitle("pic-as path");
        dialog.setHeaderText("Give the path for the pic-as assembler.");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().strip().equals("")) {
            pic_as_path = Paths.get(result.get());
        }
    }

    @FXML
    private void compile() throws IOException {
        //writing to a .asm file to compile
        String code = editor.getText();
        if(flag) {
            raiseError("Another instance running.");
            return;
        }
        try {
            File f = new File("temp/temp.asm");
            f.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(Paths.get("temp/temp.asm").toString());
            fw.write(code);
            fw.close();
        }
        catch (Exception e) {
            raiseError("Error occured while writing to a file.");
            return;
        }
        //compiling
        try {
            Process process = Runtime.getRuntime().exec(pic_as_path+" -mcpu=PIC10F200 -o"+Paths.get("temp/temp").toString()+" "+Paths.get("temp/temp.asm").toString()+" -xassembler-with-cpp");
            int exitVal = process.waitFor();
            if (exitVal == 0) {
                init_core();
                updateTables();
            }
            else raiseError("Could not be compiled.");
        }
        catch (IOException e) {
            raiseError("Path to pic-as compiler not set.\nPlease set it in the settings menu.");
        }
        catch (Exception e) {
            raiseError("Error while trying compiling");
        }
    }

    private void init_core(){
        try {
            pic = new Core("temp/temp.hex", 1);
        }
        catch (FileNotFoundException e) {
            raiseError("No such file found!");
        }
    }

    public void updateTables() {
        addr.setCellValueFactory(new PropertyValueFactory<SFR,String>("addr"));
        val.setCellValueFactory(new PropertyValueFactory<SFR,String>("val"));
        table.setItems(sfr(pic));
        addr2.setCellValueFactory(new PropertyValueFactory<SFR,String>("addr"));
        val2.setCellValueFactory(new PropertyValueFactory<SFR,String>("val"));
        table2.setItems(reg(pic));
    }

    @FXML
    public void initialize() {
        editor.clear();
        try {
            File file = new File("temp/temp.asm");
            if (file.exists() && !file.isDirectory()) {
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    editor.appendText(scan.nextLine()+"\n");
            }
                scan.close();
            }
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }
}