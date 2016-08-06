package application;
	
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.control.ChoiceBox;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class Main extends Application {
	private Stage stage;
	private Scene scene;
	
	private HashMap<String,Activite> activites=  new HashMap<String,Activite>();
	private Set<String> activitesChoisies = new HashSet<String>();
	
	private BarChart<String, Number> diagrammeDuJour;
	private LineChart<Date, Number> diagrammeEnregistrements;
	private PieChart diagrammeMoyenne ;
	
	private String xmlAdresseFichier  = "enregistrementsActivites.xml";
	
	private GridPane majActivites = new GridPane();
	private GridPane grilleActivites = new GridPane();
    private Group activitesDuJour = new Group();
    private Group enregistrements = new Group();
	private GridPane majEnregistrements = new GridPane();
	private Group moyenne = new Group();
	
	private MenuButton menuButton = new MenuButton ("Ajouter une activité");
	/*
	private Timer timer;
	private TimerTask tache;*/
	private Timeline majDureesChronos;
	private Jour jourActuel;
	
	@Override
	public void start(Stage primaryStage) {
        // définit la largeur et la hauteur de la fenêtre
        // en pixels, le (0, 0) se situe en haut à gauche de la fenêtre
        primaryStage.setWidth(700);
        primaryStage.setHeight(700);
        // met un titre dans la fenêtre
        primaryStage.setTitle("Compteur d'activités");
        this.stage = primaryStage;
        // initialisation des variables
        activites.put("Travail",new Activite("Travail","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/travail.jpg"));
		activites.put("Jeux",new Activite("Jeux","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/jeux.jpg"));
		activites.put("Détente",new Activite("Détente","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/détente.jpg"));

		Calendar date = Calendar.getInstance(Locale.FRENCH);
		jourActuel = new Jour(date);
		
        // la racine du sceneGraph est le root
        Group root = new Group();
        scene = new Scene(root,700,700);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		scene.setFill(Color.SKYBLUE);
        
        // création des onglets
        Tab tab1 = new Tab("Activités du jour"); 
        Tab tab2 = new Tab("Enregistrements"); 
        Tab tab3 = new Tab("Moyenne");
        TabPane tabPane = new TabPane(); 
        tabPane.getTabs().setAll(tab1, tab2, tab3);
        
        // ajout d'un BorderPane à l'onglet Activités du jour
        BorderPane borderPane = new BorderPane();
        tab1.setContent(borderPane);

        // ajout d'un diagramme d'activités avec moyenne à la droite du border pane
        borderPane.setRight(activitesDuJour);
        ajoutDiagrammeDuJour();    

        // ajout des activités à la gauche du border pane
        borderPane.setLeft(grilleActivites);
        ajoutActivites();
        
        // ajout de boutons en bas du border pane pour l'ajout d'activités et changer de vue
        borderPane.setBottom(majActivites);
        ajoutBoutonsMaj();
        
        // affichage des enregistrements précédents sous forme de diagramme dans l'onglet enregistrements
        BorderPane borderPaneEnregistrements = new BorderPane();
        tab2.setContent(borderPaneEnregistrements);
        borderPaneEnregistrements.setLeft(enregistrements);
        ajoutEnregistrements();
        borderPaneEnregistrements.setBottom(majEnregistrements);
        ajoutMajEnregistrements();

        // affichage de la moyenne des durées d'activités sous forme de diagramme dans l'onglet moyenne
        tab3.setContent(moyenne);
        ajoutMoyenne();
          
        // maj régulière des enregistrements
        lancerTimerMajEnregistrements();
        // lecture des enregistrements passés
        readXML(xmlAdresseFichier);
        
        // ajout des onglets à la racine du SceneGraph
		root.getChildren().add(tabPane);
        // ajout de la scène à la fenêtre
       	stage.setScene(scene);
       	stage.show();
       	
       	primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
            	Iterator<String> i = activitesChoisies.iterator();
        		while (i.hasNext()){
        			// stopper tous les chronos
        			Activite activite = activites.get(i.next());
        			activite.stopperChronoEtSauvegardeDonnees();
        		}
                saveToXML(xmlAdresseFichier);
                stopperTimerMajEnregistrements();
            }
        }); 
	}
	

	protected void stopperTimerMajEnregistrements() {
		majDureesChronos.stop();
	}

	private void lancerTimerMajEnregistrements() {

		majDureesChronos = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				// sauvegarde des valeurs si on dépasse minuit
				// et RAZ de la durée du jour
				Calendar date = Calendar.getInstance(Locale.FRENCH);
				if (!jourActuel.equals(new Jour(date))){
					// RAZ des chronos et de la durée du jour
					Iterator<String> i = activitesChoisies.iterator();
					while (i.hasNext()){
						Activite activite = activites.get(i.next());
						if (activite.estLanceChrono){
							//arrêt du chrono et sauvegarde des enregistrements
							activite.stopperChronoEtSauvegardeDonnees();
							//redémarrage du chrono
							activite.lancerChrono();
						}
						else {
							activite.enregistrerDonneesDuJour(jourActuel);
							activite.majMoyenne();
							}
						activite.setDureeDuJour(0);
					}
					jourActuel=new Jour(date);
				}
				// comptage des chronos et ajout aux diagrammes
				Iterator<String> i = activitesChoisies.iterator();
				while (i.hasNext()){
					Activite activite = activites.get(i.next());
					activite.majDureesDuJourParChrono();
				}
				// maj des diagrammes
				miseAJourMoyenne();
				miseAJourEnregistrements();
				miseAJourDiagrammeDuJour();
			}
		}));
		majDureesChronos.setCycleCount(Timeline.INDEFINITE);
		majDureesChronos.play();
	}

	private void ajoutMoyenne() { 
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
		Iterator<Activite> i = this.activites.values().iterator();
	        while (i.hasNext()){
	        	Activite activite = i.next();
	        	//defining a series
	    		pieChartData.add(new PieChart.Data(activite.nom, activite.dureeMoyenne*100/this.activites.size()));
	        }
		diagrammeMoyenne = new PieChart(pieChartData);
		diagrammeMoyenne.setTitle("Moyenne des durées des activités");
        moyenne.getChildren().add(diagrammeMoyenne);	
	}
	
	private void miseAJourMoyenne() { 
		diagrammeMoyenne.getData().clear();
		moyenne.getChildren().clear();
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
		Iterator<Activite> i = this.activites.values().iterator();
	        while (i.hasNext()){
	        	Activite activite = i.next();
	        	//defining a series
	    		pieChartData.add(new PieChart.Data(activite.nom, activite.dureeMoyenne*100/this.activites.size()));
	        }
		diagrammeMoyenne = new PieChart(pieChartData);
		diagrammeMoyenne.setTitle("Moyenne des durées des activités");
        moyenne.getChildren().add(diagrammeMoyenne);	
	}
	
	private void ajoutEnregistrements() {

		//defining the axes
        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        yAxis.setLabel("Durée");
        //creating the chart 
        diagrammeEnregistrements = new LineChart<Date,Number>(xAxis,yAxis);
        diagrammeEnregistrements.setAnimated(false);
        diagrammeEnregistrements.setTitle("Enregistrements");
        
        Iterator<String> i = this.activitesChoisies.iterator();
        while (i.hasNext()){
        	String activiteChoisie = i.next();
        	Activite activite = this.activites.get(activiteChoisie);
        	//defining a series
        	ObservableList<Data<Date, Number>> serie = FXCollections.observableArrayList() ;
        	Iterator<Entry<Jour,Long>> is = activite.dureeParJour.entrySet().iterator();
        	while (is.hasNext()){
        		Entry<Jour,Long> e= is.next();
        		//populating the series with data
                serie.add(new Data<Date,Number>(new GregorianCalendar(e.getKey().annee, e.getKey().mois, e.getKey().jour).getTime(), e.getValue()));// (new Date()).setTime(e.getValue()-(60*60*1000)));
        		
        	}
        	//SortedList<Data<String, Number>> sortedData = new SortedList<>(data, (data1, data2) -> 
        	//data1.getXValue().compareTo(data2.getXValue()));
        	diagrammeEnregistrements.getData().add(new Series<>(activite.nom,serie));
        }
        
        enregistrements.getChildren().add(diagrammeEnregistrements);

	}
	
	/*
private void ajoutEnregistrements2() {
        
		//defining the axes
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        yAxis.setLabel("Durée");
        //creating the chart
        LineChart<String, Number> diagrammeEnregistrements2 = new LineChart<String,Number>(xAxis,yAxis);
        diagrammeEnregistrements2.setAnimated(false);

        diagrammeEnregistrements2.setTitle("Enregistrements");
        Iterator<String> i = this.activitesChoisies.iterator();
        while (i.hasNext()){
        	String activiteChoisie = i.next();
        	Activite activite = this.activites.get(activiteChoisie);
        	//defining a series
        	ObservableList<Data<String, Number>> data = FXCollections.observableArrayList() ;
        	Iterator<Entry<Jour,Long>> is = activite.dureeParJour.entrySet().iterator();
        	while (is.hasNext()){
        		Entry<Jour,Long> e= is.next();
        		//populating the series with data
                //ObservableList<String> myXaxis = FXCollections.observableArrayList();
                //myXaxis.add(dateFormat.format(date));
                //xAxis.setCategories(myXaxis);
        		//addData(data, e.getKey().toString(), e.getValue());
                //data.add(new Data<String,String>(e.getKey().toString(), e.getValue()));
                data.add(new Data<String,Number>(e.getKey().toString(), e.getValue()));
        		
        	}
        	SortedList<Data<String, Number>> sortedData = new SortedList<>(data, (data1, data2) -> 
        	data1.getXValue().compareTo(data2.getXValue()));
        	diagrammeEnregistrements2.getData().add(new Series<>(activite.nom,sortedData));
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        date.setTime(e.getValue()-(60*60*1000));
        dateFormat.format(date);
        
    	diagrammeEnregistrements2.getData().get(0).getData().get(0).getYValue().
        enregistrements.getChildren().add(diagrammeEnregistrements);

	}
	private void addData(ObservableList<Data<String, Number>> data, String stringDate, Integer value) {
		Data<String, Number> dataAtDate = data.stream()
				.filter(d -> d.getXValue().equals(stringDate))
				.findAny()
				.orElseGet(() -> {
					Data<String, Number> newData = new Data<String, Number>(stringDate, 0.0);
		                data.add(newData);
		                return newData ;
		            }) ;
		        dataAtDate.setYValue(dataAtDate.getYValue().doubleValue() + value);
		        
	}*/

	private void miseAJourEnregistrements() {
		diagrammeEnregistrements.getData().clear();
		 
        Iterator<String> i = this.activitesChoisies.iterator();
        while (i.hasNext()){
        	String activiteChoisie = i.next();
        	Activite activite = this.activites.get(activiteChoisie);
        	//defining a series
        	ObservableList<Data<Date, Number>> serie = FXCollections.observableArrayList() ;
        	Iterator<Entry<Jour,Long>> is = activite.dureeParJour.entrySet().iterator();
        	while (is.hasNext()){
        		Entry<Jour,Long> e= is.next();
        		//populating the series with data
                serie.add(new Data<Date,Number>(new GregorianCalendar(e.getKey().annee, e.getKey().mois, e.getKey().jour).getTime(), e.getValue()));
        		
        	}
        	//SortedList<Data<String, Number>> sortedData = new SortedList<>(data, (data1, data2) -> 
        	//data1.getXValue().compareTo(data2.getXValue()));
        	diagrammeEnregistrements.getData().add(new Series<>(activite.nom,serie));
        }
        
	}
	
	private void ajoutMajEnregistrements() {
		Button ajoutEnregistrementButton = new Button("Ajout d'enregistrement");
		ajoutEnregistrementButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e){
				VBox vbox = new VBox(20);
				vbox.setStyle("-fx-padding: 10;");
				Scene sceneSec = new Scene(vbox, 400, 400);
				stage.setScene(sceneSec);
				// Sélection de la date de l'enregistrement
				DatePicker enregistrementsDatePicker = new DatePicker();
				enregistrementsDatePicker.setValue(LocalDate.now());
				final Callback<DatePicker, DateCell> dayCellFactory = 
						new Callback<DatePicker, DateCell>() {
					@Override
					public DateCell call(final DatePicker datePicker) {
						return new DateCell() {
							@Override
							public void updateItem(LocalDate item, boolean empty) {
								super.updateItem(item, empty);

								if (item.isAfter(LocalDate.now())){// enregistrementsDatePicker.getValue().plusDays(1))
									setDisable(true);
									setStyle("-fx-background-color: #ffc0cb;");
								}   
							}
						};
					}
				};
				enregistrementsDatePicker.setDayCellFactory(dayCellFactory);
				GridPane gridPane = new GridPane();
				gridPane.setHgap(10);
				gridPane.setVgap(10);
				Label enregistrementDateLabel = new Label("Date de l'enregistrement : ");
				gridPane.add(enregistrementDateLabel, 0, 0);
				GridPane.setHalignment(enregistrementDateLabel, HPos.LEFT);
				gridPane.add(enregistrementsDatePicker, 1, 0);
				// Entrée de la durée de l'enregistrement
				TextField enregistrementDureeTextField = new TextField();
				DecimalFormat format = new DecimalFormat( "#" );
				enregistrementDureeTextField.setTextFormatter( new TextFormatter<>(c ->
				{
				    if ( c.getControlNewText().isEmpty() )
				    {
				        return c;
				    }

				    ParsePosition parsePosition = new ParsePosition( 0 );
				    Object object = format.parse( c.getControlNewText(), parsePosition );

				    if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() )
				    {
				        return null;
				    }
				    else
				    {
				        return c;
				    }
				}));
				Label enregistrementDureeLabel = new Label("Durée de l'enregistrement : ");
				gridPane.add(enregistrementDureeLabel, 0, 1);
				GridPane.setHalignment(enregistrementDureeLabel, HPos.LEFT);
				gridPane.add(enregistrementDureeTextField, 1, 1);
				// Entrée de l'activité de l'enregistrement
				ChoiceBox<String> enregistrementActiviteChoiceBox = new ChoiceBox<String>();
				Iterator<Activite> i = activites.values().iterator(); 
				while (i.hasNext()){
					Activite activite = i.next();
					enregistrementActiviteChoiceBox.getItems().add(activite.nom);
				}
				Label enregistrementActiviteLabel = new Label("Activité de l'enregistrement : ");
				gridPane.add(enregistrementActiviteLabel, 0, 2);
				GridPane.setHalignment(enregistrementActiviteLabel, HPos.LEFT);
				gridPane.add(enregistrementActiviteChoiceBox, 1, 2);
				Button validerEnregistrement = new Button("Ok");
				validerEnregistrement.setOnAction(new EventHandler<ActionEvent>(){
					@Override public void  handle(ActionEvent e){
						Activite activite = activites.get(enregistrementActiviteChoiceBox.getValue());
						activite.dureeParJour.put(new Jour(enregistrementsDatePicker.getValue()), Long.parseLong(enregistrementDureeTextField.getText()));
						activite.lireDonneesDuJour();
				       	stage.setScene(scene);
				       	stage.show();
					}
				});
				gridPane.add(validerEnregistrement, 0, 3);
				Button annulerEnregistrement = new Button("Annuler");
				annulerEnregistrement.setOnAction(new EventHandler<ActionEvent>(){
					@Override public void  handle(ActionEvent e){
				       	stage.setScene(scene);
				       	stage.show();
					}
				});
				gridPane.add(annulerEnregistrement, 1, 3);
		        vbox.getChildren().add(gridPane);
			}
		});
		majEnregistrements.getChildren().add(ajoutEnregistrementButton);
	}

	
	/*
	 * Ajout des boutons permettant l'ajout d'activités et de changer de vue
	 * Nécessite une liste des activités possibles et une liste des activités choisies décuplées en sous-activités
	 */
	private void ajoutBoutonsMaj( ) {
		majActivites.getChildren().clear();
		menuButton.getItems().clear();
		// Ajout du menu pour ajouter une nouvelle activité avec son image et son nom
        final MenuItem nouvelleActivite = new MenuItem("Autre...");  
		nouvelleActivite.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e){
				final FileChooser dialogFileChooser = new FileChooser(); 
				dialogFileChooser.setTitle("Choix d'image pour la nouvelle activité");
				final File file = dialogFileChooser.showOpenDialog(nouvelleActivite.getParentPopup().getOwnerWindow()); 
				if (file != null) {         		
					// Ouvrir une boite de dialogue pour entrer le nom
					final TextInputDialog dialog = new TextInputDialog();  
					dialog.setTitle("Nouvelle activité");  
					dialog.setHeaderText("Choix du nom de l'activité");
					dialog.setContentText("Nom :");
					final Optional<String> result = dialog.showAndWait();  
					result.ifPresent(guestString -> {  
						// Effectuer la sauvegarde. 
						Activite activite = new Activite(guestString,file);
						//final MenuItem activiteItem = new MenuItem(activite.nom);
				        //menuButton.getItems().add(activiteItem);
				        activites.put(activite.nom,activite);
						activitesChoisies.add(activite.nom);
						ajoutActivites();
						// mettre à jour l'affichage
						miseAJourDiagrammeDuJour();
						miseAJourEnregistrements();
						ajoutBoutonsMaj();
					});
				}
			};
		});
        menuButton.getItems().add(nouvelleActivite);
        menuButton.getItems().add( new SeparatorMenuItem());
        
		// ajout du bouton pour ajouter des activités connues
		Iterator<Activite> i = this.activites.values().iterator(); 
		while (i.hasNext()){
			Activite activite = i.next();
			if(!activitesChoisies.contains(activite.nom)){
				final MenuItem activiteItem = new MenuItem(activite.nom);
				menuButton.getItems().add(activiteItem);
				// ajout des actions d'ajout sur les items du menu
				activiteItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override public void handle(ActionEvent e) {
						//ajouter l'activité sélectionnée
						activitesChoisies.add(activite.nom);
						ajoutActivites();
						// mettre à jour l'affichage
						miseAJourEnregistrements();
						miseAJourDiagrammeDuJour();
						ajoutBoutonsMaj();
					}
				});
			}
		}
		GridPane.setConstraints(menuButton,0,0);
        majActivites.getChildren().add(menuButton);
/*
        // ajout du bouton pour changer de vue
        Iterator<String> i2 = this.activitesChoisies.iterator();
        final MenuButton menuButton2 = new MenuButton ("Changer de vue");  
		while (i2.hasNext()){
			Activite activite = this.activites.get(i2.next());
			final MenuItem activiteItem = new MenuItem(activite.nom);
	        menuButton2.getItems().add(activiteItem);
		}
        GridPane.setConstraints(menuButton2,1,0);
        majActivites.getChildren().add(menuButton2);
	*/	
	}

	/*
	 * Ajout du diagramme affichant les activités du jour et la moyenne des jours passés
	 * nécessite les valeurs des compteurs de la journée et les moyennes des compteurs précédents
	 */
	private void ajoutDiagrammeDuJour() {
		final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        diagrammeDuJour = new BarChart<String,Number>(xAxis,yAxis);
        diagrammeDuJour.setAnimated(false);
        diagrammeDuJour.setTitle("Bilan d'activités");
        xAxis.setLabel("Date");       
        yAxis.setLabel("Durée");
        Iterator<String> i = this.activitesChoisies.iterator();
        while(i.hasNext()){
        	Activite activiteChoisie = this.activites.get(i.next());
        	XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
            series.setName(activiteChoisie.nom);    
            series.getData().add(new XYChart.Data<String,Number>("aujourd'hui",activiteChoisie.dureeDuJour ));
            series.getData().add(new XYChart.Data<String,Number>("jours précédents", activiteChoisie.dureeMoyenne));
            diagrammeDuJour.getData().add(series);
        	/*ObservableList<Data<String, String>> data = FXCollections.observableArrayList() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            date.setTime(activiteChoisie.dureeDuJour);
            data.add(new Data<String,String>("aujourd'hui", dateFormat.format(date).));
            date.setTime(activiteChoisie.dureeMoyenne);
            data.add(new Data<String,String>("aujourd'hui", dateFormat.format(date)));
            diagrammeDuJour.getData().add(new Series<>(activiteChoisie.nom,data));*/
        }
        activitesDuJour.getChildren().add(diagrammeDuJour);
	}

	private void miseAJourDiagrammeDuJour(){
		diagrammeDuJour.getData().clear();
        Iterator<String> i = this.activitesChoisies.iterator();
        while(i.hasNext()){
        	Activite activiteChoisie = this.activites.get(i.next());
        	XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
            series.setName(activiteChoisie.nom);       
            series.getData().add(new XYChart.Data<String,Number>("aujourd'hui", activiteChoisie.dureeDuJour+activiteChoisie.chronoTemp));
            series.getData().add(new XYChart.Data<String,Number>("jours précédents", activiteChoisie.dureeMoyenne));
            diagrammeDuJour.getData().add(series);
/*
        	ObservableList<Data<String, String>> data = FXCollections.observableArrayList() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            date.setTime(activiteChoisie.dureeDuJour);
            data.add(new Data<String,String>("aujourd'hui", dateFormat.format(date)));
            date.setTime(activiteChoisie.dureeMoyenne);
            data.add(new Data<String,String>("aujourd'hui", dateFormat.format(date)));
            diagrammeDuJour.getData().add(new Series<>(activiteChoisie.nom,data));*/
        }
	}
	
	/*
	 * Ajout des activités dans le groupe dédié
	 * nécessite le nombre des activités choisies, et le nom et l'image de chaque activité
	 */
	private void ajoutActivites() {
		grilleActivites.setHgap(6); 
		grilleActivites.setVgap(6);
		grilleActivites.getChildren().clear();
		Iterator<String> i = this.activitesChoisies.iterator();
		int cpt = 0;
		while (i.hasNext()){
			// ajout du bouton compteur à la grille d'activités
			Activite activite = this.activites.get(i.next());
			Button buttonActivite = new Button(activite.nom);
			File f = new File(activite.imageAdresseFichier);
			Image image = new Image(f.toURI().toString());
			ImageView icon = new ImageView(image); 
			icon.setFitHeight(100);
			icon.setFitWidth(100);
			icon.setPreserveRatio(true);
			buttonActivite.setGraphic(icon);
			GridPane.setConstraints(buttonActivite, 0, cpt);
			grilleActivites.getChildren().add(buttonActivite);
			cpt ++;
			// ajout des actions de compteur sur les boutons
			buttonActivite.setOnMouseClicked(
					new EventHandler<MouseEvent>(){
				@Override
				public void handle(MouseEvent e){
					if( e.getButton() == MouseButton.PRIMARY){
						//déclencher ou stopper le compteur
						activite.compter();
						// mettre à jour les diagrammes
						miseAJourDiagrammeDuJour();
						miseAJourEnregistrements();
						miseAJourMoyenne();
					}
					if ( e.getButton() == MouseButton.SECONDARY){
						activite.stopperChronoEtSauvegardeDonnees();
						grilleActivites.getChildren().remove(buttonActivite);
						activitesChoisies.remove(activite.nom);
						// mettre à jour les diagrammes
						miseAJourDiagrammeDuJour();
						miseAJourEnregistrements();
						miseAJourMoyenne();
						ajoutBoutonsMaj();
					}
				}
			});
		}
	}

	public void saveToXML(String xml) {
		Document dom;
		Element e = null;

		// instance of a DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// use factory to get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// create instance of DOM
			dom = db.newDocument();

			// create the root element
			Element rootEle = dom.createElement("activites");

			// create data elements and place them under root
			Iterator<Activite> i = this.activites.values().iterator();
			while (i.hasNext()){
				Activite activite = i.next();
				activite.enregistrerDonneesDuJour(jourActuel);
				activite.setDureeDuJour(0);
				e = dom.createElement("Activite");
				Element eNom = dom.createElement("Nom");
				eNom.appendChild(dom.createTextNode(activite.nom));
				e.appendChild(eNom);
				Element eImage = dom.createElement("ImageAdresse");
				eImage.appendChild(dom.createTextNode(activite.imageAdresseFichier));
				e.appendChild(eImage);
				Element eDureeDuJour = dom.createElement("DureeDuJour");
				eDureeDuJour.appendChild(dom.createTextNode(""+activite.dureeDuJour));
				e.appendChild(eDureeDuJour);
				Element eDureeMoyenne = dom.createElement("DureeMoyenne");
				eDureeMoyenne.appendChild(dom.createTextNode(""+activite.dureeMoyenne));
				e.appendChild(eDureeMoyenne);
				Element eDureeParJour = dom.createElement("DureeParJour");
				Iterator<Entry<Jour,Long>> is = activite.dureeParJour.entrySet().iterator();
				while (is.hasNext()){
					Entry<Jour,Long> entry= is.next();
					Element eEntry = dom.createElement("Entree");
					Element eDate = dom.createElement("Date");
					Element eJour = dom.createElement("Jour");
					eJour.appendChild(dom.createTextNode(""+entry.getKey().jour));
					eDate.appendChild(eJour);
					Element eMois = dom.createElement("Mois");
					eMois.appendChild(dom.createTextNode(""+entry.getKey().mois));
					eDate.appendChild(eMois);
					Element eAnnee = dom.createElement("Annee");
					eAnnee.appendChild(dom.createTextNode(""+entry.getKey().annee));
					eDate.appendChild(eAnnee);
					eEntry.appendChild(eDate);
					Element eDuree = dom.createElement("Duree");
					eDuree.appendChild(dom.createTextNode(""+entry.getValue().longValue()));
					eEntry.appendChild(eDuree);
					eDureeParJour.appendChild(eEntry);
				}
				e.appendChild(eDureeParJour);
				rootEle.appendChild(e);
			}
			Iterator<String> i2 = this.activitesChoisies.iterator();
			while(i2.hasNext()){
				String activiteChoisie = i2.next();
				e = dom.createElement("ActiviteChoisie");
				e.appendChild(dom.createTextNode(activiteChoisie));
				rootEle.appendChild(e);
			}

			dom.appendChild(rootEle);

			try {
				Transformer tr = TransformerFactory.newInstance().newTransformer();
				tr.setOutputProperty(OutputKeys.INDENT, "yes");
				tr.setOutputProperty(OutputKeys.METHOD, "xml");
				tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				//tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
				tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

				// send DOM to file
				tr.transform(new DOMSource(dom), 
						new StreamResult(new FileOutputStream(xml)));

			} catch (TransformerException te) {
				System.out.println(te.getMessage());
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		} catch (ParserConfigurationException pce) {
			System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
		}
	}

	public boolean readXML(String xml) {
		Document doc;
		// Make an  instance of the DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// use the factory to take an instance of the document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using the builder to get the DOM mapping of the    
			// XML file
			doc = db.parse(xml);
			doc.getDocumentElement().normalize();
			NodeList nListActivites = doc.getElementsByTagName("Activite");
			for (int temp = 0; temp < nListActivites.getLength(); temp++) {
				Node nNode = nListActivites.item(temp);

				// variables de récupération de valeurs
				String nom;
				String imageAdresseFichier;
				long dureeDuJour;
				long dureeMoyenne ; 
				HashMap<Jour,Long> dureeParJour = new HashMap<Jour,Long>();

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					nom = eElement.getElementsByTagName("Nom").item(0).getTextContent();
					imageAdresseFichier = eElement.getElementsByTagName("ImageAdresse").item(0).getTextContent();
					dureeDuJour = Long.valueOf(eElement.getElementsByTagName("DureeDuJour").item(0).getTextContent()).longValue();
					dureeMoyenne = Long.parseLong(eElement.getElementsByTagName("DureeMoyenne").item(0).getTextContent());

					NodeList nListEntrees = eElement.getElementsByTagName("Entree");
					for (int temp2 = 0; temp2 < nListEntrees.getLength(); temp2++) {
						Node nNodeEntree = nListEntrees.item(temp2);
						if (nNodeEntree.getNodeType() == Node.ELEMENT_NODE) {
							Element eEntree = (Element) nNodeEntree;

							int jour;
							int mois;
							int annee;
							Node nDate = eEntree.getElementsByTagName("Date").item(0);
							Element eDate = (Element) nDate;
							jour = Integer.parseInt(eDate.getElementsByTagName("Jour").item(0).getTextContent());
							mois = Integer.parseInt(eDate.getElementsByTagName("Mois").item(0).getTextContent());
							annee = Integer.parseInt(eDate.getElementsByTagName("Annee").item(0).getTextContent());
							Jour date = new Jour(jour,mois,annee);
							long duree = Long.parseLong(eEntree.getElementsByTagName("Duree").item(0).getTextContent());
							dureeParJour.put(date, new Long(duree));
						}

						Activite activite = new Activite(nom,imageAdresseFichier,dureeDuJour,dureeMoyenne,dureeParJour);
						activite.lireDonneesDuJour();
						activite.enregistrerDonneesDuJour(jourActuel);
						activite.majMoyenne();
						this.activites.put(activite.nom,activite);	
						

					}

				}
			}
			NodeList nListActivitesChoisies = doc.getElementsByTagName("ActiviteChoisie");
			for (int temp = 0; temp < nListActivitesChoisies.getLength(); temp++) {
				Node nNode = nListActivitesChoisies.item(temp);
				String activiteChoisie = ((Element) nNode).getTextContent();		
				this.activitesChoisies.add(activiteChoisie);
			}
			ajoutBoutonsMaj();
			ajoutActivites();
			// mettre à jour les diagrammes
			miseAJourDiagrammeDuJour();
			miseAJourEnregistrements();
			miseAJourMoyenne();
		} catch (ParserConfigurationException pce) {
			System.out.println(pce.getMessage());
		} catch (SAXException se) {
			System.out.println(se.getMessage());
		} catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		return false;
	}
	public static void main(String[] args) {
		launch(args);
	}
}
