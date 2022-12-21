import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.Optional;
public class Controller {

    private String pic_as_path = "pic-as";
    Core pic =null;

    @FXML
    private TableView<SFR> table, table2;

    @FXML
    private TableColumn<SFR, String> addr, val, addr2, val2;

    @FXML
    private TextArea editor;

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
                data.add(new SFR(addr, val));
            }
        });
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
        System.out.println("run");
    }

    @FXML
    private void step() {
        System.out.println("step");
        if(pic == null) raiseError("Compile before stepping or running.");
        else {
            pic.step();
            updateTable();
        }
    }

    @FXML
    private void openSearch() {
        editor.clear();
        try {
            FileChooser fc = new FileChooser();
            File file = fc.showOpenDialog(table.getScene().getWindow());
            if (file != null) {
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()) {
                    editor.appendText(scan.nextLine()+"\n");
                }
                scan.close();
            }
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }

    private void raiseError(String err) {
        Alert alert = new Alert(null);
        alert.setTitle("alert");
        alert.setContentText(err);
        ButtonType bt = new ButtonType("Close");
        alert.getButtonTypes().add(bt);
        alert.show();
    }

    @FXML
    private void compile() throws IOException {
        String code = editor.getText();
        try {
            FileWriter fw = new FileWriter("out/production/Emulator/temp.asm");
            fw.write(code);
            fw.close();
        }
        catch (Exception e) {System.out.println(e.getMessage());}
        //compiling
        try {
            Process process = Runtime.getRuntime().exec(pic_as_path+" -mcpu=PIC10F200 -oout/production/Emulator/temp out/production/Emulator/temp.asm -xassembler-with-cpp");
            StringBuilder output = new StringBuilder();

		    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		    String line;
		    while ((line = reader.readLine()) != null) {
			    output.append(line + "\n");
		    }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                init();
                updateTable();
            }
            else raiseError("Could not compile");
        }
        catch (Exception e) {System.out.println("Error !!!!");}
    }

    private void init() throws FileNotFoundException{
        try {
            pic = new Core("out/production/Emulator/temp.hex", 1);
        }
        catch (FileNotFoundException e) {
            raiseError("No such file found!");
        }
    }

    public void updateTable() {
        addr.setCellValueFactory(new PropertyValueFactory<SFR,String>("addr"));
        val.setCellValueFactory(new PropertyValueFactory<SFR,String>("val"));
        table.setItems(sfr(pic));
        addr2.setCellValueFactory(new PropertyValueFactory<SFR,String>("addr"));
        val2.setCellValueFactory(new PropertyValueFactory<SFR,String>("val"));
        table2.setItems(reg(pic));
    }

    @FXML
    private void getPath() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("pic-as path");
        dialog.setHeaderText("Give the path for the pic-as assembler.");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().equals("")) {
            pic_as_path = result.get();
        }

    }

    @FXML
    public void initialize() {
    }
}