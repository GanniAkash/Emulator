import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import java.util.HashMap;

public class Controller {

    @FXML
    private TableView<SFR> table;

    @FXML
    private TableColumn<SFR, String> addr, val;;

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

    public ObservableList<SFR> sfr() {
        ObservableList<SFR> data = FXCollections.observableArrayList();
        Core pic = new Core("test.hex", 1);
        HashMap<String, String> map = pic.registers;
        map.forEach((addr, val) -> {
            if (val == null) val = "null";
            data.add(new SFR(addr, val));
        });
        return data;
    }

    @FXML
    private void createTable() {
        System.out.println("hi");
    }

    @FXML
    public void initialize() {
        addr.setCellValueFactory(new PropertyValueFactory<SFR,String>("addr"));
        val.setCellValueFactory(new PropertyValueFactory<SFR,String>("val"));
        table.setItems(sfr());
    }
}