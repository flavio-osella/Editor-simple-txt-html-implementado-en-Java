import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import javax.swing.undo.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.Color;


public class Editor2 extends JFrame{

	JFileChooser fileChooser = null;//navegador de archivos
	JEditorPane panelEditor;
	JScrollPane scrollPanelDerecho;
	JScrollPane scrollPanelIzquierdo;
	JPanel panelIzquierdo; //donde van los archivos abiertos

	JMenuBar menuBar;
	JMenu archivoMenu;
	JMenu ayudaMenu;
	JMenu verMenu;
	JMenuItem nuevoMenu;
	JMenuItem abrirMenu;
	JMenuItem salvarMenu;
	JMenuItem salirMenu;
	JMenuItem acercaDeMenu;
	JMenuItem oscuroMenu;
	JMenuItem normalMenu;
	JComboBox tamañoMenu;
	JComboBox tipoMenu;


	JSplitPane splitPane; //divide en dos el panel

	JPanel statusPanel;//panel de estado y mensajes
	JLabel statusMsg1;
	JLabel statusMsg2;

	JToolBar toolBar;

	JButton botonNuevo;
	JButton botonAbrir;
	JButton botonGuardar;
	JButton botonCortar;
	JButton botonPegar;
	JButton botonCopiar;
	JButton botonAbrirSeleccion;//en panel izquierdo
	JButton botonDes;
	JButton botonRe;
	JButton botonBuscar;
	

	JList lista; //lista que se despliega en lado izquierdo

	Document editorPaneDocument;
	UndoHandler undoHandler= new UndoHandler();
	UndoManager undoManager= new UndoManager();
	UndoAction undoAction= null;
	RedoAction redoAction= null;
	DefaultHighlighter hilit = new DefaultHighlighter();
	DefaultHighlighter.DefaultHighlightPainter pintar = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);


	Vector archivoVector= new Vector(); //vector para almacenar los archivos abiertos
	String nombreArchivo;
	boolean esSalvado= false;
	String ultimaBusqueda= "";
	

	public Editor2(String titulo){ //constructor
		super(titulo);
	}

	class EventHandler implements ActionListener{

		public void actionPerformed(ActionEvent e){

			fuente();

			if(e.getSource()== salirMenu){
				if(!esSalvado && panelEditor.getText().equals("")){
					System.exit(0);
				}
				if(!esSalvado && !panelEditor.getText().equals("")){ //el doc no esta salvado ni es vacio
					salvar();
					System.exit(0);
				}
				if(esSalvado && !panelEditor.getText().equals("")){
					System.exit(0);
				}
				if(esSalvado && panelEditor.getText().equals("")){
					System.exit(0);
				}				
			}
			if(e.getSource()== abrirMenu || e.getSource()== botonAbrir){


				if(!esSalvado && !panelEditor.getText().equals("")){
					salvar();
					abrirArchivo();
				}
				if(!esSalvado && panelEditor.getText().equals("")){
					abrirArchivo();
				}
				
			}
			if(e.getSource()== nuevoMenu || e.getSource()== botonNuevo){

				
				if(!esSalvado && panelEditor.getText().equals("")){
					nuevoArchivo();
				}
				if(!esSalvado && !panelEditor.getText().equals("")){
					salvar();
					nuevoArchivo();
				}				
				if(esSalvado && panelEditor.getText().equals("")){
					nuevoArchivo();
				}
			}
			if(e.getSource()== salvarMenu || e.getSource()== botonGuardar){
				salvar();
			}
			if(e.getSource()== botonCopiar){
				panelEditor.copy();
			}
			if(e.getSource()== botonCortar){
				panelEditor.cut();
			}
			if(e.getSource()== botonPegar){
				panelEditor.paste();
			}
			if(e.getSource()== botonAbrirSeleccion){
				abrirArchivoSeleccionado();
			}
			if(e.getSource()== acercaDeMenu){
				JOptionPane.showMessageDialog(null,"F Editor"+"\n"+ "Editor formato txt"+"\n" +" Autor:Flavio Osella"+"\n"+"Córdoba, Argentina");
			}
			if(e.getSource()== botonDes){
				try{
					undoManager.undo();
				}
				catch (Exception ex){

				}
				
			}
			if(e.getSource()== botonRe){
				try{
					undoManager.redo();
				}
				catch (Exception ex){

				}
			}
			if(e.getSource()== botonBuscar){

				buscar();
			}
			if(e.getSource()== oscuroMenu){
				try{
					oscuro();
				}
				catch(Exception ex){

				}
			}
			if(e.getSource()== normalMenu){

				try{
					normal();
				}
				catch(Exception ex){

				}
			}
		};
	};

	ActionListener eventos = new EventHandler();

	public void agregaLista(String archivo){

		if(archivoVector.contains(archivo)){
			return;
		}
		archivoVector.add(archivo);
		Collections.sort(archivoVector);
		lista.setListData(archivoVector);
	}

	public void salvar(){

		JOptionPane panelOpciones= new JOptionPane();
		Object[] opciones= {"SI","NO"};

		int ret= panelOpciones.showOptionDialog(this, "Desea Guardar?", "Pregunta", JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,opciones,opciones[0]);

		if(ret== JOptionPane.YES_OPTION){
			salvarArchivo();
		}
	}

	public void init(){

		nuevoMenu.addActionListener(eventos);
		abrirMenu.addActionListener(eventos);
		salvarMenu.addActionListener(eventos);
		salirMenu.addActionListener(eventos);
		oscuroMenu.addActionListener(eventos);
		normalMenu.addActionListener(eventos);
		botonNuevo.addActionListener(eventos);
		botonAbrir.addActionListener(eventos);
		botonGuardar.addActionListener(eventos);
		botonCopiar.addActionListener(eventos);
		botonCortar.addActionListener(eventos);
		botonPegar.addActionListener(eventos);
		botonAbrirSeleccion.addActionListener(eventos);
		tamañoMenu.addActionListener(eventos);
		tipoMenu.addActionListener(eventos);
		acercaDeMenu.addActionListener(eventos);
		botonDes.addActionListener(eventos);
		botonRe.addActionListener(eventos);
		botonBuscar.addActionListener(eventos);
		

		editorPaneDocument = panelEditor.getDocument();
		editorPaneDocument.addUndoableEditListener(undoHandler);
		
	}

	public void initialize(){

		this.getContentPane().setLayout(new BorderLayout());

		this.addWindowListener(new WindowAdapter(){   //clase anonima cuando se aprieta x guarda archivo
			public void windowClosing(WindowEvent e){
				if(!esSalvado && !panelEditor.getText().equals("")){
					salvar();
					System.exit(0);
				}
			}
		});

		menuBar= new JMenuBar();
		archivoMenu= new JMenu("Archivo");
		nuevoMenu= new JMenuItem("Nuevo");
		abrirMenu= new JMenuItem("Abrir");
		salvarMenu= new JMenuItem("Guardar");
		salirMenu= new JMenuItem("Salir");
		verMenu= new JMenu("Ver");
		oscuroMenu= new JMenuItem("Modo Oscuro");
		normalMenu= new JMenuItem("Modo Normal");
		ayudaMenu= new JMenu("Ayuda");
		acercaDeMenu= new JMenuItem("Acerca de F Editor");


		archivoMenu.add(nuevoMenu);
		archivoMenu.add(abrirMenu);
		archivoMenu.add(salvarMenu);
		archivoMenu.addSeparator();
		archivoMenu.add(salirMenu);

		verMenu.add(oscuroMenu);
		verMenu.add(normalMenu);

		ayudaMenu.add(acercaDeMenu);

		menuBar.add(archivoMenu);
		menuBar.add(verMenu);
		menuBar.add(ayudaMenu);

		this.setJMenuBar(menuBar);

		toolBar= new JToolBar();

		tamañoMenu= new JComboBox();
		tamañoMenu.setMaximumSize(new Dimension(50, 30));
		
		for(int i= 1; i<=40; i++){
			tamañoMenu.addItem(String.valueOf(i));
		}

		tipoMenu= new JComboBox();
		//metodo para obtener y poner las fuentes en un arreglo
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fonts = ge.getAvailableFontFamilyNames();
		for (String string : fonts) {  //mete en el combobox las fuentes
            tipoMenu.addItem(string);
        }
        tipoMenu.setMaximumSize(new Dimension(100, 30));
        tamañoMenu.setSelectedIndex(17);



		botonNuevo= new JButton();
		botonNuevo.setIcon(new ImageIcon(getClass().getResource("/nuevo.png")));
		botonNuevo.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonNuevo);

		botonAbrir= new JButton();
		botonAbrir.setIcon(new ImageIcon(getClass().getResource("/abrir.png")));
		botonAbrir.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonAbrir);

		botonGuardar= new JButton();
		botonGuardar.setIcon(new ImageIcon(getClass().getResource("/guardar.png")));
		botonGuardar.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonGuardar);

		toolBar.addSeparator();

		botonCopiar= new JButton();
		botonCopiar.setIcon(new ImageIcon(getClass().getResource("/copiar.png")));
		botonCopiar.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonCopiar);

		botonCortar= new JButton();
		botonCortar.setIcon(new ImageIcon(getClass().getResource("/cortar.png")));
		botonCortar.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonCortar);

		botonPegar= new JButton();
		botonPegar.setIcon(new ImageIcon(getClass().getResource("/pegar.png")));
		botonPegar.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonPegar);

		toolBar.addSeparator();

		botonDes= new JButton();
		botonDes.setIcon(new ImageIcon(getClass().getResource("/atras.png")));
		botonDes.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonDes);

		botonRe= new JButton();
		botonRe.setIcon(new ImageIcon(getClass().getResource("/reponer.png")));
		botonRe.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonRe);

		toolBar.addSeparator();

		toolBar.add(tamañoMenu);

		toolBar.add(tipoMenu);

		toolBar.addSeparator();

		botonBuscar= new JButton();
		botonBuscar.setIcon(new ImageIcon(getClass().getResource("/buscar.png")));
		botonBuscar.setMargin(new Insets(0,0,0,0));
		toolBar.add(botonBuscar);
		


		this.getContentPane().add(toolBar, BorderLayout.NORTH);

		statusPanel= new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusMsg1= new JLabel("Estado: ");
		statusMsg2= new JLabel();
		statusPanel.add(statusMsg1, BorderLayout.WEST);
		statusPanel.add(statusMsg2, BorderLayout.CENTER);

		this.getContentPane().add(statusPanel, BorderLayout.SOUTH);

		panelEditor= new JEditorPane();
		panelEditor.setText("");
		scrollPanelDerecho= new JScrollPane(panelEditor);

		lista= new JList();
		lista.setBackground(UIManager.getColor("Button.background"));
		scrollPanelIzquierdo= new JScrollPane(lista);
		botonAbrirSeleccion= new JButton("Abrir");

		panelIzquierdo= new JPanel(new BorderLayout());
		panelIzquierdo.add(scrollPanelIzquierdo, BorderLayout.CENTER);
		panelIzquierdo.add(botonAbrirSeleccion, BorderLayout.SOUTH);

		splitPane= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setRightComponent(scrollPanelDerecho);
		splitPane.setLeftComponent(panelIzquierdo);

		KeyStroke undoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.META_MASK);
		KeyStroke redoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.META_MASK);

		undoAction = new UndoAction();
		panelEditor.getInputMap().put(undoKeystroke, "undoKeystroke");
		panelEditor.getActionMap().put("undoKeystroke", undoAction);

		redoAction = new RedoAction();
		panelEditor.getInputMap().put(redoKeystroke, "redoKeystroke");
		panelEditor.getActionMap().put("redoKeystroke", redoAction);

		

		this.getContentPane().add(splitPane, BorderLayout.CENTER);

	}

	

	public void fuente(){

		String tipoFuente= tipoMenu.getSelectedItem().toString();
		int tamañoFuente= tamañoMenu.getSelectedIndex() + 1;
		panelEditor.setFont(new Font(tipoFuente, Font.PLAIN, tamañoFuente));

	}

	public static void main(String[] args) {
		
		Editor2 aplicacion= new Editor2("F Editor");
		aplicacion.initialize();
		aplicacion.init();
		aplicacion.pack();
		aplicacion.setSize(700,400);
		aplicacion.setLocation(100,100);
		aplicacion.setVisible(true);

	}

	public void nuevoArchivo(){

		panelEditor.setText("");
		esSalvado= false;
	}

	public void abrirArchivo(){

		if(fileChooser== null){
			fileChooser= new JFileChooser();
		}
		int retVal= fileChooser.showOpenDialog(this);
		if(retVal== fileChooser.APPROVE_OPTION){
			nombreArchivo= fileChooser.getSelectedFile().getAbsolutePath();
			try{
				java.net.URL url= fileChooser.getSelectedFile().toURL();
				statusMsg2.setText("Abriendo "+ nombreArchivo);

				panelEditor.setPage(url);
				esSalvado= false;
				agregaLista(""+ nombreArchivo);
			}
			catch (Exception ioe){
				statusMsg2.setText(ioe.getMessage());
			}				
		}
		
	}

	public void abrirArchivoSeleccionado(){

		if(lista.getSelectedIndex()== -1){
			return;
		}
		int indice= lista.getSelectedIndex();
		try{
			String nombre= (String)archivoVector.get(indice);
			java.net.URL url= (new java.io.File(nombre)).toURL();
			panelEditor.setPage(url);
		}
		catch (Exception e){

			statusMsg2.setText(e.getMessage());
		}
	}

	public void buscar(){
   
        panelEditor.setHighlighter(hilit);

        hilit.removeAllHighlights();
       
        String text = JOptionPane.showInputDialog(null,"Texto: ","F Editor - Buscar",JOptionPane.QUESTION_MESSAGE);
        if (text != null) {    //si se introdujo texto (puede ser una cadena vacía)
 	        String textoDoc = panelEditor.getText();    //obtiene todo el contenido del área de edición
            int pos = textoDoc.indexOf(text); 
            int end = pos + text.length();
            try{
            	if (pos > -1) {    
            	hilit.addHighlight(pos, end, pintar);
                panelEditor.setCaretPosition(end);
            	}
        	}
            catch(BadLocationException e){
                    e.printStackTrace();
            }   
           //establece el texto buscado como el texto de la última búsqueda realizada
            ultimaBusqueda = text;
        }
    }

	public void salvarArchivo(){

		if(fileChooser== null){
			fileChooser= new JFileChooser();
		}
		int retVal= fileChooser.showSaveDialog(this);
		if(retVal== fileChooser.APPROVE_OPTION){
			nombreArchivo= fileChooser.getSelectedFile().getAbsolutePath();
			try{

				statusMsg2.setText("Guardando "+ nombreArchivo);
				String texto= panelEditor.getText();
				java.io.FileWriter escribeArchivo= new java.io.FileWriter(nombreArchivo);
				java.io.BufferedWriter br = new java.io.BufferedWriter(escribeArchivo);
				br.write(texto);
				br.close();
				esSalvado= true;
				agregaLista(""+ nombreArchivo);
			}
			catch (Exception ioe){
				statusMsg2.setText(ioe.getMessage());
			}
		}
	}

	public void oscuro(){

		panelEditor.setBackground(Color.BLACK);
		panelEditor.setForeground(Color.WHITE);
		menuBar.setBackground(Color.BLACK);
		menuBar.setForeground(Color.WHITE);
		archivoMenu.setBackground(Color.BLACK);
		archivoMenu.setForeground(Color.WHITE);
		verMenu.setBackground(Color.BLACK);
		verMenu.setForeground(Color.WHITE);
		ayudaMenu.setBackground(Color.BLACK);
		ayudaMenu.setForeground(Color.WHITE);
		toolBar.setBackground(Color.BLACK);
		tipoMenu.setBackground(Color.BLACK);
		tipoMenu.setForeground(Color.WHITE);
		tamañoMenu.setBackground(Color.BLACK);
		tamañoMenu.setForeground(Color.WHITE);
		botonNuevo.setBackground(Color.BLACK);
		botonAbrir.setBackground(Color.BLACK);
		botonGuardar.setBackground(Color.BLACK);
		botonCopiar.setBackground(Color.BLACK);
		botonCortar.setBackground(Color.BLACK);
		botonPegar.setBackground(Color.BLACK);
		botonBuscar.setBackground(Color.BLACK);
		botonRe.setBackground(Color.BLACK);
		botonDes.setBackground(Color.BLACK);
		botonAbrirSeleccion.setBackground(Color.BLACK);
		botonAbrirSeleccion.setForeground(Color.WHITE);
		panelIzquierdo.setBackground(Color.BLACK);
		panelIzquierdo.setForeground(Color.WHITE);
		lista.setBackground(Color.BLACK);
		lista.setForeground(Color.WHITE);
		fileChooser.setBackground(Color.BLACK);
		fileChooser.setForeground(Color.WHITE);
		scrollPanelIzquierdo.setBackground(Color.BLACK);
		scrollPanelDerecho.setBackground(Color.BLACK);
	}

	public void normal(){

		panelEditor.setBackground(Color.WHITE);
		panelEditor.setForeground(Color.BLACK);
		menuBar.setBackground(UIManager.getColor("Button.background"));
		menuBar.setForeground(Color.BLACK);
		archivoMenu.setBackground(UIManager.getColor("Button.background"));
		archivoMenu.setForeground(Color.BLACK);
		verMenu.setBackground(UIManager.getColor("Button.background"));
		verMenu.setForeground(Color.BLACK);
		ayudaMenu.setBackground(UIManager.getColor("Button.background"));
		ayudaMenu.setForeground(Color.BLACK);
		toolBar.setBackground(UIManager.getColor("Button.background"));
		tipoMenu.setBackground(UIManager.getColor("Button.background"));
		tipoMenu.setForeground(Color.BLACK);
		tamañoMenu.setBackground(UIManager.getColor("Button.background"));
		tamañoMenu.setForeground(Color.BLACK);
		botonNuevo.setBackground(UIManager.getColor("Button.background"));
		botonAbrir.setBackground(UIManager.getColor("Button.background"));
		botonGuardar.setBackground(UIManager.getColor("Button.background"));
		botonCopiar.setBackground(UIManager.getColor("Button.background"));
		botonCortar.setBackground(UIManager.getColor("Button.background"));
		botonPegar.setBackground(UIManager.getColor("Button.background"));
		botonBuscar.setBackground(UIManager.getColor("Button.background"));
		botonRe.setBackground(UIManager.getColor("Button.background"));
		botonDes.setBackground(UIManager.getColor("Button.background"));
		botonAbrirSeleccion.setBackground(UIManager.getColor("Button.background"));
		botonAbrirSeleccion.setForeground(Color.BLACK);
		panelIzquierdo.setBackground(UIManager.getColor("Button.background"));
		panelIzquierdo.setForeground(Color.BLACK);
		lista.setBackground(UIManager.getColor("Button.background"));
		lista.setForeground(Color.BLACK);
		fileChooser.setBackground(UIManager.getColor("Button.background"));
		fileChooser.setForeground(Color.BLACK);
		scrollPanelIzquierdo.setBackground(UIManager.getColor("Button.background"));
		scrollPanelDerecho.setBackground(UIManager.getColor("Button.background"));

	}

	class UndoHandler implements UndoableEditListener{ //clases de deshacer y rehacer

  		public void undoableEditHappened(UndoableEditEvent e){
    
    		undoManager.addEdit(e.getEdit());
    		undoAction.update();
    		redoAction.update();
  		}
	}

	class UndoAction extends AbstractAction{
 		public UndoAction(){
    		super("Undo");
   			setEnabled(false);
  	}

 		public void actionPerformed(ActionEvent e){

 			
			try{
 				undoManager.undo();
			}
			catch (CannotUndoException ex){
      
			}
			update();
			redoAction.update();   		
  		}

  		protected void update(){
    		if (undoManager.canUndo()){
      			setEnabled(true);
      			putValue(Action.NAME, undoManager.getUndoPresentationName());
    		}
    		else{
      			setEnabled(false);
      			putValue(Action.NAME, "Undo");
    		}
  		}
	}

	class RedoAction extends AbstractAction{
  		public RedoAction(){
    		super("Redo");
    		setEnabled(false);
  		}

  		public void actionPerformed(ActionEvent e){
    		try{
      			undoManager.redo();
    		}
    		catch (CannotRedoException ex){
      // TODO deal with this
      			ex.printStackTrace();
    		}
    		update();
    		undoAction.update();
  		}

  		protected void update(){
    		if (undoManager.canRedo()){
      			setEnabled(true);
      			putValue(Action.NAME, undoManager.getRedoPresentationName());
    		}
    		else{
     			setEnabled(false);
      			putValue(Action.NAME, "Redo");
   			 }
  		}
	}	
}