import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
        private final StringProperty addr = new SimpleStringProperty();
        private final StringProperty val = new SimpleStringProperty();

        public final StringProperty addrProperty() {
            return addr;
        }

        public final String getAddr() {
            return addr.get();
        }

        public final void setAddr(String s) {
            addr.set(s);
        }

        public final StringProperty valProperty() {
            return val;
        }

        public final String getVal() {
            return val.get();
        }

        public final void setVal(String s) {
            val.set(s);
        }

        public SFR (String s1, String s2) {
            setAddr(s1);
            setVal(s2);
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