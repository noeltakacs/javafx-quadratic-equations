/*
 * Quadratic equation
 * Noel Takács
 * 3.D
 */

//importovanie potrebných knižníc a tried na tvorbu aplikácie
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.event.EventHandler;
import javafx.event.Event;
import java.text.DecimalFormat;
import java.io.PrintWriter;
import java.io.FileWriter;
import javafx.scene.shape.Circle;
import javafx.scene.input.ScrollEvent;

public class App extends Application{
  //pomocou konštanty som si zadefinoval rozlíšenie okna (konštanta preto, lebo sa rozlíšenie počas behu meniť nebude)
  public static final int WIDTH = 1280;
  public static final int HEIGHT = 720;
  //pomocou konštanty som si zadefinoval počet riadkov a stĺpcov v GridPane (konštanta preto, lebo sa počet riadkov a stĺpcov počas behu meniť nebude)
  public static final int ROWNUM = 20;
  public static final int COLNUM = 6;

  //triedu DecimalFormat som využil na prevedenie desatinného čísla do daného formátu
  private static final DecimalFormat decfor = new DecimalFormat("0.00");
  
  //deklarácia jednotlivých objektov na tvorbu aplikácie
  private Label lb_title, lb_coefA, lb_coefB, lb_coefC, lb_root1, lb_root2, lb_discriminant, lb_root1result, lb_root2result, lb_discriminantOutput, lb_format;
  private TextField tf_coefA, tf_coefB, tf_coefC;
  private Button bt_Calculate, bt_clear;
  private RadioButton rb_light, rb_dark;
  private CheckBox cb_grid, cb_txt;
  private Circle dotX1, dotX2;
  private LineChart<Number, Number> lineChart;
  private XYChart.Series<Number, Number> series;
  private GridPane gridPane;

  private int zoomLevel = 100; //Základný (defaultný) zoom je nastavený na 100, táto dátová položka sa bude používať na úpravu zoomu
  private boolean twoResultsVisible = true; //obyčajná dátova položka typu boolean, ktorá uchováva aktuálny stav, či je zobrazené x1 a x2 alebo iba x1 resp. x
  
  //Vytvorenie súradnicového systému X a Y, nastavenie rozsahu a "stepu/ticku"
  final NumberAxis xAxis = new NumberAxis(-10, 10, 1);
  final NumberAxis yAxis = new NumberAxis(-10, 10, 1);

  public void initGUI(Stage stage){
    //Vytváranie Labelov a priraďovanie triedy
    lb_title = new Label("QuadCalc");
    lb_title.setId("title");
    lb_coefA = new Label("a = ");
    lb_coefB = new Label("b = ");
    lb_coefC = new Label("c = ");
    lb_discriminant = new Label("D = ");
    lb_discriminantOutput = new Label();
    lb_discriminantOutput.getStyleClass().add("output");
    lb_root1 = new Label("x\u2081 = ");
    lb_root1result = new Label();
    lb_root1result.getStyleClass().add("output");
    lb_root2 = new Label("x\u2082 = ");
    lb_root2result = new Label();
    lb_root2result.getStyleClass().add("output");
    lb_format = new Label("ax\u00B2+bx+c=0");
    
    //Vytváranie TextFieldov a priraďovanie triedy
    tf_coefA = new TextField();
    tf_coefA.getStyleClass().add("field");
    tf_coefB = new TextField();
    tf_coefB.getStyleClass().add("field");
    tf_coefC = new TextField();
    tf_coefC.getStyleClass().add("field");
    
    //Vytvorenie Buttonov a priraďovanie triedy
    bt_Calculate = new Button("Calculate");
    bt_Calculate.getStyleClass().add("calculate");
    bt_Calculate.setOnAction(new ButtonClick()); //volanie metódy setOnAction, aby sa po kliknutí tlačidla vykonala metóda z vnorenej triedy

    bt_clear = new Button("Clear");
    bt_clear.getStyleClass().add("clear");
    bt_clear.setAlignment(Pos.BOTTOM_RIGHT); //nastavenie umiestnenia tlačidla v danej kolónke gridu
    bt_clear.setOnAction(new EventHandler(){ //anonymná trieda, po kliknutí na tlačidlo sa zavolá metóda, ktorá resetuje aplikáciu
      public void handle(Event event){
        clearAll(); //volanie metódy na mazanie outputov a grafu
      }
    });

    //Vytvorenie CheckBoxu a priraďovanie triedy
    cb_grid = new CheckBox("Grid Lines");
    cb_grid.getStyleClass().add("checkbox");
    cb_grid.setSelected(true); //Nastavenie checkboxu, aby bol defaultne zakliknutý (zaklinutý po štarte aplikácie)
    cb_txt = new CheckBox("Txt output");
    cb_txt.getStyleClass().add("checkbox");
    cb_txt.setSelected(true); //Nastavenie checkboxu, aby bol defaultne zakliknutý (zaklinutý po štarte aplikácie)

    cb_grid.setOnAction(new EventHandler(){ //Anonymná trieda, ktorá pri zmene stavu checkboxu zavolá metódu
      public void handle(Event event){
        if(cb_grid.isSelected()){ //Podmienka kontroluje, či je checkbox zaškrtnutý a na základe výsledku skryje alebo zobrazí pomocné čiary v grafe
          lineChart.setHorizontalGridLinesVisible(true);
          lineChart.setVerticalGridLinesVisible(true);
        }else{
          lineChart.setHorizontalGridLinesVisible(false);
          lineChart.setVerticalGridLinesVisible(false);
        }
      }
    });

    //Vytvorenie RadioButtonov a priraďovanie triedy
    ToggleGroup toggleGroup = new ToggleGroup(); //Vytvorenie skupiny pre RadioButtony, aby nemohli byť obe zakliknuté naraz
    rb_light = new RadioButton("Light Mode");
    rb_dark = new RadioButton("Dark Mode");
    rb_light.setToggleGroup(toggleGroup); //Priradenie do skupiny
    rb_dark.setToggleGroup(toggleGroup);
    rb_light.setSelected(true); //Nastavenie radiobuttonu, aby bol predvolený (zaklinutý po štarte aplikácie)
    rb_dark.getStyleClass().add("theme-switch");
    rb_light.getStyleClass().add("theme-switch");

    //Vytváranie dvoch bodiek, ktoré budú vedľa Labelov x1 a x2 (signalizujú, že aké farby predstavujú x1 a x2 v grafe)
    dotX1 = new Circle(5, Color.DEEPSKYBLUE);
    dotX2 = new Circle(5, Color.LIME);

    //Vytvorenie GridPanu, nastavenie triedy a maximálnej šírky
    gridPane = new GridPane();
    gridPane.setGridLinesVisible(false);
    gridPane.setVgap(5);
    gridPane.getStyleClass().add("grid-pane");
    gridPane.setMaxWidth(400);

    //Zaradenie objektov do gridu, nastavenie pozície
    gridPane.add(lb_format, 0, 15, 6, 1);
    gridPane.add(lb_coefA, 1, 4, 1, 1);
    gridPane.add(lb_coefB, 1, 5, 1, 1);
    gridPane.add(lb_coefC, 1, 6, 1, 1);
    gridPane.add(tf_coefA, 2, 4, 3, 1);
    gridPane.add(tf_coefB, 2, 5, 3, 1);
    gridPane.add(tf_coefC, 2, 6, 3, 1);
    gridPane.add(lb_discriminant, 1, 8, 1, 1);
    gridPane.add(lb_discriminantOutput, 2, 8, 3, 1);
    gridPane.add(lb_root1, 1, 10, 1, 1);
    gridPane.add(lb_root2, 1, 11, 1, 1);
    gridPane.add(lb_root1result, 2, 10, 3, 1);
    gridPane.add(lb_root2result, 2, 11, 3, 1);
    gridPane.add(bt_Calculate, 2, 13, 2, 1);
    gridPane.add(bt_clear, 5, 19, 5, 1);
    gridPane.add(rb_light, 0, 18, 2, 1);
    gridPane.add(rb_dark, 0, 19, 2, 1);
    gridPane.add(cb_grid, 0, 17, 2, 1);
    gridPane.add(cb_txt, 0, 16, 2, 1);
    gridPane.add(dotX1, 0, 10, 1, 1);
    gridPane.add(dotX2, 0, 11, 1, 1);
    GridPane.setHalignment(bt_clear, HPos.CENTER); //upravovanie polohy objektu v kolónke gridu
    GridPane.setHalignment(bt_Calculate, HPos.CENTER);
    GridPane.setHalignment(lb_format, HPos.CENTER);
    GridPane.setHalignment(dotX1, HPos.CENTER);
    GridPane.setHalignment(dotX2, HPos.CENTER);
    
    //Cyklus, ktorý vytvorí požadovaný počet stĺpcov
    for (int i = 0; i < COLNUM; i++) { //Cyklus bude bežať, až kým vytvorí všetky stĺpce
      ColumnConstraints colConst = new ColumnConstraints(); //Objekt, v ktorom bude uchovaný stĺpec s daným rozmerom
      colConst.setPercentWidth(100.0 / COLNUM); //Matematická operácia, ktorá vypočíta a nastaví veľkosť každého stĺpca, aby stĺpce mali rovnakú šírku
      gridPane.getColumnConstraints().add(colConst); //Pridávanie stĺpcov do gridu
    }
    
    //Cyklus, ktorý vytvorí požadovaný počet riadkov
    for (int i = 0; i < ROWNUM; i++) { //Cyklus bude bežať, až kým vytvorí všetky riadky
      RowConstraints rowConst = new RowConstraints(); //Objekt, v ktorom bude uchovaný riadok s daným rozmerom
      rowConst.setPercentHeight(100.0 / ROWNUM); //Matematická operácia, ktorá vypočíta a nastaví veľkosť každého riadku, aby riadky mali rovnakú výšku
      gridPane.getRowConstraints().add(rowConst); //Pridávanie riadkov do gridu
    }
    
    //Vytvorenie VBoxu a priradenie Labelu, úprava polohy labelu na stred
    VBox vbox = new VBox();
    vbox.getChildren().add(lb_title);
    vbox.setAlignment(Pos.CENTER);
    vbox.getStyleClass().add("v-box");

    //Vytvorenie LineChartu s osou X a Y, ktoré sme si vytvorili na začiatku
    lineChart = new LineChart<>(xAxis, yAxis); //LineChart som zvolil kvôli tomu, že budeme kresliť grafy kvadratických a lineárnych rovníc
    lineChart.getStyleClass().add("line-chart");
    lineChart.setCreateSymbols(false); //Metóda, ktorá skryje body na grafe, aby nezavadzali
    lineChart.setLegendVisible(false); //Metóda, ktorá skryje legendu grafu
    lineChart.setOnScroll(new Zoom()); //Volanie metódy setOnScroll, aby sa po scrollovaní grafu vykonala metóda vo vnorenej triede Zoom

    //Vytvorenie BorderPanu, ktorý sa postará o správne uloženie vytvorených častí (Ľavý panel - grid, Horný panel - vbox(názov) a stredný panel - graf)
    BorderPane borderPane = new BorderPane();
    borderPane.setTop(vbox);
    borderPane.setLeft(gridPane);
    borderPane.setCenter(lineChart);
    
    //Vytvorenie scény s nami definovaným rozlíšením, priradenie CSS, nastavenie ikony a nastavenie okna aplikácie
    Scene scene = new Scene(borderPane, WIDTH, HEIGHT);
    stage.setTitle("Quadratic equation");
    stage.setScene(scene);
    stage.getIcons().add(new Image("/img/icon.png")); //Pridávanie ikony (ikona - objekt triedy Image)
    scene.getStylesheets().add("css/style-light.css");
    stage.setResizable(true); //Povolenie zväčšovať a zmenšovať okno
    stage.setMaximized(true); //Automaticky nastaví najväčšiu veľkosť okna

    rb_light.setOnAction(new EventHandler(){ //Anonymná trieda s metódou, ktorá sa stará o prepínanie light módu a dark módu
      public void handle(Event event){
        scene.getStylesheets().remove("css/style-dark.css");
        scene.getStylesheets().add("css/style-light.css");
      }
    });

    rb_dark.setOnAction(new EventHandler(){ //Zmena vzhľadu je urobená pomocou jednoduchého princípu, odstráni sa jedno CSS a  priradí sa druhé
      public void handle(Event event){
        scene.getStylesheets().remove("css/style-light.css");
        scene.getStylesheets().add("css/style-dark.css");
      }
    });
    
    stage.show(); //Zobrazenie celého okna (pódia) 
  }
  
  public void start(Stage stage){ //Metóda na spustenie aplikácie
    initGUI(stage); //Volanie metódy, ktorá vytvorí celé GUI
  }

  public static void main(String arg[]){ //Metóda main (v tomto prípade nie je povinná)
    Application.launch(arg); //Spúšťanie aplikácie, ktorá dokáže preberať parametre aj z cmd
  }

  class ButtonClick implements EventHandler{ //Vnorená trieda s metódou handle, ktorá sa vykoná po kliknutí na tlačidlo
    public void handle(Event event){
      //Na začiatku nastaví všetky outputy na prázdny string
      lb_discriminantOutput.setText("");
      lb_root1result.setText("");
      lb_root2result.setText("");
      lb_format.setText(tf_coefA.getText() + "x\u00B2+" + tf_coefB.getText() + "x+" + tf_coefC.getText() + "=0"); //Výpis kvadratickej rovnice s aktuálnymi hodnotami

      series = new XYChart.Series<>(); //Vytvorenie grafu
      lineChart.getData().clear(); //Vymazanie všetkých grafov z LineChartu, aby ich tam nebolo viac
      
      try{ //Pokiaľ sa objaví error v bloku "try", tak sa vykoná blok "catch"
        //Načítanie vstupov do premenných na jednoduchšiu prácu s danými hodnotami
        float coefA = Float.parseFloat(tf_coefA.getText());
        float coefB = Float.parseFloat(tf_coefB.getText());
        float coefC = Float.parseFloat(tf_coefC.getText());
        float discriminant = Discriminant.discriminant(coefA, coefB, coefC); //Zavolanie statickej metódy, ktorá nám z daných hodnôt vypočíta diskriminant
        //Zavolanie statickej metódy, ktorá nám vráti pole výsledkov (x1, x2)
        float result[] = QuadraticEquation.quadraticEquation(coefA, coefB, coefC); //x1 (alebo x) má stále index 0 a x2 má stále index 2
        
        lb_discriminantOutput.setText(String.valueOf(decfor.format(discriminant))); //Nastavenie textu políčka na hodnotu diskriminantu 
        
        if(result == null){ //Pokiaľ nám metóda na počítanie rovnice vráti NULL (D < 0), tak program vyhodí daný error
          showAlert("No solution", "The value of the discriminant is a negative number. This equation has no solution in the set of real numbers.");
          lb_root1result.setText("No solution");
          lb_root2result.setText("No solution");
          return; //Return musí byť, inak by sa metóda vykonávala ďalej a vyhodilo by to aj druhý error
        }
        
        if(result.length == 1){ //Podmienka, ktorá kontroluje či je veľkosť pola 1 (rovnica má iba jedno riešenie)
          if(twoResultsVisible == true){ //Podmienka skontroluje, či je viditeľné x1 a x2 alebo iba x1 resp. x (na základe výsledku podmienky vykonáva ďalšie príkazy)
            lb_root1.setText("x = "); //Zmení sa názov Labelu
            gridPane.getChildren().removeAll(lb_root2, lb_root2result, dotX2); //Odstránia sa dané Labely z gridu
          }
          
          lb_root1result.setText(String.valueOf(decfor.format(result[0]))); //Nastavenie textu Labelu na hodnotu výsledku rovnice

          if(discriminant != 0){ //Podmienka kontroluje, či je D väčší ako 0 (pokiaľ áno, tak je to lineárna rovnica(diskriminant lineárnej rovnice je stále coefB * coefB)) 
            lb_discriminantOutput.setText(""); //Pri lineárnej rovnici je diskriminant zbytočny, preto sa nezobrazí
          }else{ //Pokiaľ diskriminant bude 0, tak nám to signalizuje, že rovnica má iba jedno riešenie, alebo je konštanta
            if(coefA == 0 && coefB == 0){ //pokiaľ búdú koeficienty A aj B rovné 0, graf bude iba jedna rovná čiara (konštanta) bez riešenia
              lb_discriminantOutput.setText("");
              lb_root1result.setText("No solution");
            }
          }
          twoResultsVisible = false; //Po vykonaní tejto metódy nebude viditeľná kolónka pre x1 a x2 (iba x1 resp. x), tak potrebujeme aktualizovať stav, aby nenastali problémy 
        }else{ //Pokiaľ má pole veľkosť väčšiu ako 1 (to znamená 2), tak má rovnica 2 riešenia (x1 a x2)
          if(twoResultsVisible == false){ //Podmienka skontroluje, či je viditeľné x1 a x2 alebo iba x1 resp. x (na základe výsledku podmienky vykonáva ďalšie príkazy)
            lb_root1.setText("x\u2081 = "); //Zmení sa názov Labelu
            gridPane.getChildren().addAll(lb_root2, lb_root2result, dotX2); //Pridajú sa dané Labely do gridu
          }

          lb_root1result.setText(String.valueOf(decfor.format(result[0]))); //Text daného Labelu sa nastaví na hodnotu x1
          lb_root2result.setText(String.valueOf(decfor.format(result[1]))); //Text daného Labelu sa nastaví na hodnotu x2

          twoResultsVisible = true; //Po vykonaní tejto metódy bude viditeľná kolónka pre x1 aj x2, tak potrebujeme aktualizovať stav, aby nenastali problémy
        }

        drawGrap(series, coefA, coefB, coefC); //Volanie metódy, ktorá nám vykreslí graf
        txtOutput(coefA, coefB, coefC, result, twoResultsVisible); //volanie metódy na výpis do texťáku
        addPoint(series, coefA, coefB, coefC, result, twoResultsVisible); //volanie metódy na vykreslenie bodov na grafe 
        
        lineChart.getData().add(series); //pridanie grafu do linechartu
      }catch(Exception e){ //Pokiaľ v bloku "try" nastane chyba (chýba input alebo chybný input), tak to vyhodí error s daným textom
        showAlert("Wrong input", "Some fields are left blank or contain a value of the wrong type. The input must be a number!");
        lb_format.setText("ax\u00B2+bx+c=0");
      }
    }
  }

  //Vnorená trieda, v ktorej sa nachádza metóda handle, ktorá sa volá pri zoomovaní
  public class Zoom implements EventHandler<ScrollEvent>{ //EventHandler musí byť špecifikovaný, aby som mohol pristupovať k metódam eventu, ako je getDeltaY
    public void handle(ScrollEvent event){ //Handle sa zavolá v prípade, že sa objaví nejaký scroll event (zoom)
      if(event.getDeltaY() > 0 && zoomLevel > 100){ //Podmienka kontroluje, či používateľ od-zoomuje (slúži na to getDeltaY) | Taktiež kontroluje, aby používateľ neprekročil základný (defaultný) zoom level
        zoomLevel -= 10; //od-zoomovanie (oddiaľovanie)
      }else if(event.getDeltaY() < 0){ //Podmienka kontroluje, či používateľ zoomuje (slúži na to metóda getDeltaY) | V tomto prípade hranica na maximálny zoom neexistuje, takže používateľ si môže graf približovať koľko chce
        zoomLevel += 10; //zoomovanie (približovanie)
      }
  
      //Hranice osi X a Y sa vynásobia levelom zoomu, aby sme dostali nové väčšie/menšie hranice
      xAxis.setLowerBound(-0.1 * zoomLevel); //Číslo (-0.1) upravuje, o koľko sa zväčšia/zmenšia hranice (čím väčšie, tým väčší krok)
      xAxis.setUpperBound(0.1 * zoomLevel); //Pri základnom zoome (100) je zväčšenie 1x | Pri náraste zoomu sa zväčšuje aj miera zoomu (napr. 2x, 3x, 10x, ...)

      yAxis.setLowerBound(-0.1 * zoomLevel);
      yAxis.setUpperBound(0.1 * zoomLevel);
    }
  }

  //Metóda, ktorá vyhodí alert s custom názvom a textom
  public void showAlert(String title, String body){ //Metóda slúži na uľahčenie práce s alertmi (nepotrebujem vytvárať osobitne alert pre každý prípad)
    Alert alert = new Alert(AlertType.ERROR); //Vytvorenie alertu typu ERROR
    alert.setHeaderText(title); //Nastavenie názvu a tela alertu na základe preberaných parametrov
    alert.setContentText(body);

    //Vytvorenie objektu triedy ImageView pomocou konštruktora s jedným parametrom, ktorý bude zvolený obrázok
    ImageView imageView = new ImageView(new Image("/img/icon2.png"));
    imageView.setFitHeight(50); //Nastavenie výšky a šírky obrázku
    imageView.setFitWidth(50);

    //Priradenie CSS-ka a triedy, nastavenie šírky a výšky a pridanie obrázku
    alert.getDialogPane().getStyleClass().add("alert");
    alert.getDialogPane().getStylesheets().add("css/alert.css");
    alert.getDialogPane().setPrefSize(480, 290);
    alert.setGraphic(imageView); //Nastavenie obrázka v alerte

    //Vytvorenie "pódia" pre alert, aby sme mohli zmeniť ikonu
    Stage alertStage = (Stage)alert.getDialogPane().getScene().getWindow();
    alertStage.getIcons().add(new Image("/img/icon2.png"));
    
    alertStage.show(); //Vyhodí alert
  }

  public void clearAll(){ //Jednoduchá metóda, ktorá vymaže všetky inputy, outputy a graf (nahradí ich prázdnym stringom)
    tf_coefA.setText("");
    tf_coefB.setText("");
    tf_coefC.setText("");
    lb_discriminantOutput.setText("");
    lb_root1result.setText("");
    lb_root2result.setText("");
    lb_format.setText("ax\u00B2+bx+c=0");
    lineChart.getData().clear(); //Mazanie všetkých grafov

    if(twoResultsVisible == false){ //Podmienka skontroluje, či je viditeľné x1 a x2 alebo iba x1 resp. x (na základe výsledku podmienky vykonáva ďalšie príkazy)
      lb_root1.setText("x\u2081 = ");
      gridPane.getChildren().addAll(lb_root2, lb_root2result, dotX2); //Priradenie objektov do gridu
    }

    twoResultsVisible = true; //Po vykonaní tejto metódy bude viditeľná kolónka pre x1 aj x2, tak potrebujeme aktualizovať stav, aby nenastali problémy
  }

  public void txtOutput(float coefA, float coefB, float coefC, float[] result,boolean twoResults) { //Metóda na zapisovanie do txt súboru
    PrintWriter pw = null; //Definícia mimo bloku try, aby bola metóda viditeľná aj v bloku catch
    if(cb_txt.isSelected()){ //Podmienka, ktorá kontrolju či je zaškrtnutý checkbox
      try{ //try-catch pre prípad, keby sa vyskytla nejaká chyba pri vypisovaní, tak nevypíše a zatvorí .txt súbor     
        pw = new PrintWriter(new FileWriter("output.txt", true)); //Vytváranie objektu, ktorý bude zapisovať do súboru, ktorý mu odovzdávame v parametroch
        pw.println("a = " + decfor.format(coefA));
        pw.println("b = " + decfor.format(coefB));
        pw.println("c = " + decfor.format(coefC));
        pw.println("");
        pw.println("D = " + decfor.format(Discriminant.discriminant(coefA, coefB, coefC)));
        pw.println("");
        if(twoResults == true){ //Podmienka, ktorá kontroluje, či má rovnica 1 alebo 2 riešenia
          pw.println("x\u2081 = " + decfor.format(result[0]));
          pw.println("x\u2082 = " + decfor.format(result[1]));
        }else{
          if(coefA == 0 && coefB == 0){ //Podmienka, ktorá kontroluje, či má rovnica výsledok pre x
            pw.println("x = No solution");
          }else{ //Pokiaľ sa nevykonala ani jedna podmienka, tak má rovnica iba 1 riešenie (rovnica je buď lineárna alebo D == 0)
            pw.println("x = " + decfor.format(result[0]));
          }
        }
        pw.println("---------");
        pw.close(); //zatvorenie .txt súboru
      }catch(Exception e){ //Pokiaľ sa vyskytne chyba, tak sa zatvorí .txt súbor
        pw.close();
      }
    }
  }

  public void addPoint(XYChart.Series<Number, Number> series, float coefA, float coefB, float coefC, float[] result,boolean twoResults) { //metóda, ktorá vykreslí body na pozíciu x1 a x2 (dôležitý parameter je XYChart.Series -> graf na ktorý bude vykreslovať body)
    Data<Number, Number> dotX1 = new Data<Number, Number>(result[0], QuadraticEquation.findY(coefA, coefB, coefC, result[0])); //Objekt, ktorý bude uchovávať polohu bodu pre x1 (preberá 2 parametre -> polohu na osi X, čiže výsledok rovnice a polohu Y, ktorú vypočíta statická metóda)
    dotX1.setNode(new Circle(5, Color.DEEPSKYBLUE)); //nastavenie vzhľadu bodu
    series.getData().add(dotX1); //pridanie bodu do grafu

    if(twoResults == true){ //Podmienka, ktorá kontroluje, či má rovnica 2 riešenia (ak áno, tak vytvorí bod aj pre x2)
      Data<Number, Number> dotX2 = new Data<Number, Number>(result[1], QuadraticEquation.findY(coefA, coefB, coefC, result[1])); //Objekt, ktorý bude uchovávať polohu bodu pre x1 (preberá 2 parametre -> polohu na osi X, čiže výsledok rovnice a polohu Y, ktorú vypočíta statická metóda)
      dotX2.setNode(new Circle(5, Color.LIME)); //nastavenie vzhľadu bodu
      series.getData().add(dotX2); //pridanie bodu do grafu
    }
  }

  public void drawGrap(XYChart.Series<Number, Number> series, float coefA, float coefB, float coefC){ //Metóda na vykresľovanie grafu do série, ktorá sa preberá ako parameter
    if(coefA == 0){ //Podmienka kontroluje, či je graf lineárny (pokiaľ hej, tak na výpočet Y použije metódu určenú pre lineárne rovnice)
      for(float i = -100; i < 100; i += 0.1){ //Cyklus na kreslenie grafu bod po bode (i = x)
        series.getData().add(new XYChart.Data(i, LinearEquation.findY(coefB, coefC, i))); //pre každé X-ko sa pomocou statickej metódy vypočíta Y a daný bod sa vykreslí
      }
    }else if(coefA == 0 && coefB == 0){ //Podmienka kontroluje, či je výsledok rovnice konštanta (žiadne riešenie v x iba rovná čiara cez os Y)
      for(float i = -100; i < 100; i += 0.1){ //Cyklus na kreslenie grafu bod po bode (i = x)
        series.getData().add(new XYChart.Data(i, coefC)); //Pre každé X-ko a Y (Y je coefC a nemení sa), takže nám vznikne rovná čiara, ktorá sa nepretína s osou X
      }
    }else{ //Pokiaľ by sa nevykonala ani jedna z vyšších blokov, tak sme zistili, že rovnica je kvadratická s 1 alebo 2 riešeniami
      for(float i = -100; i < 100; i += 0.1){ //Cyklus na kreslenie grafu bod po bode (i = x)
        series.getData().add(new XYChart.Data(i, QuadraticEquation.findY(coefA, coefB, coefC, i))); //pre každé X-ko sa pomocou statickej metódy vypočíta Y a daný bod sa vykreslí
      }
    }
  }
}