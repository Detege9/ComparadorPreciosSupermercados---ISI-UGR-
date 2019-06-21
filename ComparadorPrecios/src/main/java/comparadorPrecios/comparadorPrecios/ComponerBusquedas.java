package comparadorPrecios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;



//Clase encargada de integrar la informaci�n de las tres fuentes de datos.
//La informaci�n de dos de las fuentes de datos se consigue mediante web scraping. La restante se consigue
//accediendo a la API de Amazon.
public class ComponerBusquedas {
	String producto;
	ArrayList<Producto> listaProductos = new ArrayList<Producto>();
	
	public ComponerBusquedas(String str) {
		this.producto = str;
	}
	
	//Este m�todo ordena los elementos de la clase Producto de menor a mayor en base al atributo precio.
	Comparator<Producto> compareByPrice = new Comparator<Producto>() {
		@Override
		public int compare(Producto p1, Producto p2) {
			float precio1 = p1.getPrecio();
			float precio2 = p2.getPrecio();
			
			return Float.compare(precio1, precio2);
		}
	};
	
	//Esta funci�n crea un objeto de la clase Scraping y llama a la funci�n buscar().
	//Esta funci�n recopila la informaci�n de las dos fuentes de datos en una lista de productos que 
	//recogemos en nuestra listaProductos.
	private void busquedaScraping() throws IOException {
		Scraping s = new Scraping(producto);
		s.buscar();
		
		listaProductos = s.getListaProductos();
	}
	
	//Esta funci�n crea un objeto de la clase JavaCodeSnippet y llama a la funci�n buscarAmazon(). 
	//La funci�n recoge la informaci�n de los productos y los va a�adiendo a un vector de productos.
	//Ese vector de productos se a�ade a nuestra listaProductos.
	private void busquedaAmazon() throws IOException, ParserConfigurationException, SAXException {
		JavaCodeSnippet jcs = new JavaCodeSnippet(producto);
		jcs.buscarAmazon();
		
		listaProductos.addAll(jcs.getListaProductos());	
	}

	public ArrayList<Producto> getListaProductos(){
    	return this.listaProductos;
    }
	
	//Es la funci�n que llama a las dos funciones principales de esta clase. 
	//Por �ltimo, ordena la lista de productos compar�ndolos por el atributo precio.
	public void busqueda() throws IOException, ParserConfigurationException, SAXException {
		busquedaScraping();
		busquedaAmazon();
		
		Collections.sort(listaProductos, compareByPrice);
	}
	
	
}
