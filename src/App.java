import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Emulator");
        
        Parent root = FXMLLoader.load(getClass().getResource("javafx/BasicApplication_i18n.fxml"));
        Scene scene = new Scene(root); 
        stage.setTitle("Emulator");
        stage.setMinHeight(480);
        stage.setMinWidth(1000);
        stage.setMaxHeight(480);
        stage.setMaxWidth(1000); 
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}