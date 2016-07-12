package application;
	
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class Main extends Application {
	private  Set<Activite> activites=  new HashSet<Activite>();;
	private Set<Activite> activitesChoisies = new HashSet<Activite>();
	private BarChart<String,Number> diagrammeDuJour;
	private LineChart<Number,Number> diagrammeEnregistrements;
	private PieChart diagrammeMoyenne ;
	private String xmlAdresseFichier ;
	@Override
	public void start(Stage primaryStage) {
        // définit la largeur et la hauteur de la fenêtre
        // en pixels, le (0, 0) se situe en haut à gauche de la fenêtre
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        // met un titre dans la fenêtre
        primaryStage.setTitle("Compteur d'activités");
        
        // initialisation des variables
        activites.add(new Activite("Travail","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/travail.jpg"));
		activites.add(new Activite("Jeux","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/jeux.jpg"));
		activites.add(new Activite("Détente","C:/Users/asus/workspace/CompteurdActivitesJavaFX/src/application/ressources/détente.jpg"));
	
		this.xmlAdresseFichier = "enregistrementsActivites.xml";
		
        // la racine du sceneGraph est le root
        Group root = new Group();
        Scene scene = new Scene(root,1200,800);
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
        Group activitesDuJour = new Group();
        borderPane.setRight(activitesDuJour);
        ajoutDiagrammeDuJour(activitesDuJour);    

        // affichage de la moyenne des durées d'activités sous forme de diagramme dans l'onglet moyenne
        Group moyenne = new Group();
        tab3.setContent(moyenne);
        ajoutMoyenne(moyenne);
        
        // ajout des activités à la gauche du border pane
        GridPane grilleActivites = new GridPane();
        borderPane.setLeft(grilleActivites);
        ajoutActivites(grilleActivites,moyenne);
        
        // ajout de boutons en bas du border pane pour l'ajout d'activités et changer de vue
        GridPane majActivites = new GridPane();
        borderPane.setBottom(majActivites);
        ajoutBoutonsMaj(majActivites,this.activitesChoisies,this.activites,grilleActivites,moyenne);
        
        // affichage des enregistrements précédents sous forme de diagramme dans l'onglet enregistrements
        Group enregistrements = new Group();
        tab2.setContent(enregistrements);
        ajoutEnregistrements(enregistrements);
        

        readXML(xmlAdresseFichier,grilleActivites,moyenne);
        // ajout des onglets à la racine du SceneGraph
		root.getChildren().add(tabPane);
        // ajout de la scène à la fenêtre
       	primaryStage.setScene(scene);
       	primaryStage.show();
       	
       	primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                saveToXML(xmlAdresseFichier);
            }
        }); 
	}
	
	private void ajoutMoyenne(Group moyenne) { 
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
		Iterator<Activite> i = this.activites.iterator();
	        while (i.hasNext()){
	        	Activite activite = i.next();
	        	//defining a series
	    		pieChartData.add(new PieChart.Data(activite.nom, activite.dureeMoyenne*100/this.activites.size()));
	        }
		diagrammeMoyenne = new PieChart(pieChartData);
		diagrammeMoyenne.setTitle("Moyenne des durées des activités");
        moyenne.getChildren().add(diagrammeMoyenne);	
	}
	
	private void miseAJourMoyenne(Group moyenne) { 
		diagrammeMoyenne.getData().clear();
		moyenne.getChildren().clear();
		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
		Iterator<Activite> i = this.activites.iterator();
	        while (i.hasNext()){
	        	Activite activite = i.next();
	        	//defining a series
	    		pieChartData.add(new PieChart.Data(activite.nom, activite.dureeMoyenne*100/this.activites.size()));
	        }
		diagrammeMoyenne = new PieChart(pieChartData);
		diagrammeMoyenne.setTitle("Moyenne des durées des activités");
        moyenne.getChildren().add(diagrammeMoyenne);	
	}
	
	private void ajoutEnregistrements(Group enregistrements) {
		//defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        //creating the chart
        diagrammeEnregistrements = new LineChart<Number,Number>(xAxis,yAxis);
                
        diagrammeEnregistrements.setTitle("Enregistrements");
        Iterator<Activite> i = this.activites.iterator();
        while (i.hasNext()){
        	Activite activite = i.next();
        	//defining a series
            XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
            series.setName(activite.nom);
            Iterator<Entry<Jour,Integer>> is = activite.dureeParJour.entrySet().iterator();
            while (is.hasNext()){
            	Entry<Jour,Integer> e= is.next();
            	//populating the series with data
                series.getData().add(new XYChart.Data<Number,Number>(e.getKey().toNumber(), e.getValue()));
            }
            diagrammeEnregistrements.getData().add(series);
        }
        
        enregistrements.getChildren().add(diagrammeEnregistrements);
		
	}
	
	private void miseAJourEnregistrements() {
		diagrammeEnregistrements.getData().clear();
        Iterator<Activite> i = this.activites.iterator();
        while (i.hasNext()){
        	Activite activite = i.next();
        	//defining a series
            XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
            series.setName(activite.nom);
            Iterator<Entry<Jour,Integer>> is = activite.dureeParJour.entrySet().iterator();
            while (is.hasNext()){
            	Entry<Jour,Integer> e= is.next();
            	//populating the series with data
                series.getData().add(new XYChart.Data<Number,Number>(e.getKey().toNumber(), e.getValue()));
            }
            diagrammeEnregistrements.getData().add(series);
        }		
	}
	/*
	 * Ajout des boutons permettant l'ajout d'activités et de changer de vue
	 * Nécessite une liste des activités possibles et une liste des activités choisies décuplées en sous-activités
	 */
	private void ajoutBoutonsMaj(GridPane majActivites,Set<Activite> activitesChoisies2,Set<Activite> activites, GridPane grilleActivites,Group moyenne ) {

        final MenuButton menuButton = new MenuButton ("Ajouter une activité"); 
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
						final MenuItem activiteItem = new MenuItem(activite.nom);
				        menuButton.getItems().add(activiteItem);
				        activites.add(activite);
						activitesChoisies2.add(activite);
						ajoutActivites(grilleActivites,moyenne);
					});
				}
			};
		});
        menuButton.getItems().add(nouvelleActivite);
        menuButton.getItems().add( new SeparatorMenuItem());
		// ajout du bouton pour ajouter des activités
		Iterator<Activite> i = this.activites.iterator(); 
		while (i.hasNext()){
			Activite activite = i.next();
			final MenuItem activiteItem = new MenuItem(activite.nom);
	        menuButton.getItems().add(activiteItem);
	        // ajout des actions d'ajout sur les items du menu
            activiteItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                	//ajouter l'activité sélectionnée
                	activitesChoisies2.add(activite);
                	ajoutActivites(grilleActivites,moyenne);
                }
            });
		}
        GridPane.setConstraints(menuButton,0,0);
        majActivites.getChildren().add(menuButton);

        // ajout du bouton pour changer de vue
        Iterator<Activite> i2 = this.activitesChoisies.iterator();
        final MenuButton menuButton2 = new MenuButton ("Changer de vue");  
		while (i2.hasNext()){
			Activite activite = i2.next();
			final MenuItem activiteItem = new MenuItem(activite.nom);
	        menuButton.getItems().add(activiteItem);
		}
        GridPane.setConstraints(menuButton2,1,0);
        majActivites.getChildren().add(menuButton2);
		
	}

	/*
	 * Ajout du diagramme affichant les activités du jour et la moyenne des jours passés
	 * nécessite les valeurs des compteurs de la journée et les moyennes des compteurs précédents
	 */
	private void ajoutDiagrammeDuJour(Group activitesDuJour) {
		final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        diagrammeDuJour = new BarChart<String,Number>(xAxis,yAxis);
        diagrammeDuJour.setAnimated(false);
        diagrammeDuJour.setTitle("Bilan d'activités");
        xAxis.setLabel("Date");       
        yAxis.setLabel("Durée");
        Iterator<Activite> i = this.activitesChoisies.iterator();
        while(i.hasNext()){
        	Activite activiteChoisie = i.next();
        	XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
            series.setName(activiteChoisie.nom);       
            series.getData().add(new XYChart.Data<String,Number>("aujourd'hui", activiteChoisie.dureeDuJour));
            series.getData().add(new XYChart.Data<String,Number>("jours précédents", activiteChoisie.dureeMoyenne));
            diagrammeDuJour.getData().add(series);
        }
        activitesDuJour.getChildren().add(diagrammeDuJour);
	}

	private void miseAJourDiagrammeDuJour(){
		diagrammeDuJour.getData().clear();
        Iterator<Activite> i = this.activitesChoisies.iterator();
        while(i.hasNext()){
        	Activite activiteChoisie = i.next();
        	XYChart.Series<String,Number> series = new XYChart.Series<String,Number>();
            series.setName(activiteChoisie.nom);       
            series.getData().add(new XYChart.Data<String,Number>("aujourd'hui", activiteChoisie.dureeDuJour));
            series.getData().add(new XYChart.Data<String,Number>("jours précédents", activiteChoisie.dureeMoyenne));
            diagrammeDuJour.getData().add(series);
        }
	}
	
	/*
	 * Ajout des activités dans le groupe dédié
	 * nécessite le nombre des activités choisies, et le nom et l'image de chaque activité
	 */
	private void ajoutActivites(GridPane grilleActivites,Group moyenne) {
		grilleActivites.setHgap(6); 
		grilleActivites.setVgap(6);
		grilleActivites.getChildren().clear();
		Iterator<Activite> i = this.activitesChoisies.iterator();
		int cpt = 0;
        while (i.hasNext()){
        	// ajout du bouton compteur à la grille d'activités
        	Activite activite = i.next();
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
            buttonActivite.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                	//déclencher ou stopper le compteur
                	activite.compter();
                	// mettre à jour les diagrammes
                	miseAJourDiagrammeDuJour();
                	miseAJourEnregistrements();
                	miseAJourMoyenne(moyenne);
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
	        Iterator<Activite> i = this.activitesChoisies.iterator();
	        while (i.hasNext()){
	        	Activite activite = i.next();
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
		        Iterator<Entry<Jour,Integer>> is = activite.dureeParJour.entrySet().iterator();
	            while (is.hasNext()){
	            	Entry<Jour,Integer> entry= is.next();
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
	            	eDuree.appendChild(dom.createTextNode(""+entry.getValue().intValue()));
	            	eEntry.appendChild(eDuree);
	            	eDureeParJour.appendChild(eEntry);
	            }
	            e.appendChild(eDureeParJour);
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

	public boolean readXML(String xml,GridPane grilleActivites,Group moyenne) {
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
			Element eDoc = doc.getDocumentElement();
			NodeList nListActivites = doc.getElementsByTagName("Activite");
			for (int temp = 0; temp < nListActivites.getLength(); temp++) {
				Node nNode = nListActivites.item(temp);

				// variables de récupération de valeurs
				String nom;
				String imageAdresseFichier;
				long dureeDuJour;
				int dureeMoyenne ; 
				HashMap<Jour,Integer> dureeParJour = new HashMap<Jour,Integer>();

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					nom = eElement.getElementsByTagName("Nom").item(0).getTextContent();
					imageAdresseFichier = eElement.getElementsByTagName("ImageAdresse").item(0).getTextContent();
					dureeDuJour = Long.valueOf(eElement.getElementsByTagName("DureeDuJour").item(0).getTextContent()).longValue();
					dureeMoyenne = Integer.parseInt(eElement.getElementsByTagName("DureeMoyenne").item(0).getTextContent());

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
							int duree = Integer.parseInt(eEntree.getElementsByTagName("Duree").item(0).getTextContent());
							dureeParJour.put(date, new Integer(duree));
						}

						Activite activite = new Activite(nom,imageAdresseFichier,dureeDuJour,dureeMoyenne,dureeParJour);
						this.activitesChoisies.add(activite);
					}

				}
			}

        	ajoutActivites(grilleActivites,moyenne);
        	// mettre à jour les diagrammes
        	miseAJourDiagrammeDuJour();
        	miseAJourEnregistrements();
        	miseAJourMoyenne(moyenne);
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
