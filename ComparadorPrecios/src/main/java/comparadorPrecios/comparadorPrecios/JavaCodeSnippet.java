package comparadorPrecios;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.w3c.dom.Element;

/*
 * This class shows how to make a simple authenticated call to the
 * Amazon Product Advertising API.
 *
 * See the README.html that came with this sample for instructions on
 * configuring and running the sample.
 */

//Clase encargada de realizar la conexi�n con la API de Amazon y extraer la informaci�n
//que necesitamos.
public class JavaCodeSnippet {
		
    /*
     * Your Access Key ID, as taken from the Your Account page.
     */
    private static final String ACCESS_KEY_ID = "AKIAJ2M7Z62GFWKITXBA";

    /*
     * Your Secret Key corresponding to the above ID, as taken from the
     * Your Account page.
     */
    private static final String SECRET_KEY = "ASJo63DLv+0IaNwTp1j4ytzQvkUaM+AXNlbAOBVa";

    /*
     * Use the end-point according to the region you are interested in.
     */
    private static final String ENDPOINT = "webservices.amazon.es";
    
    private String producto;
    
    private ArrayList<Producto> listaProductos = new ArrayList<Producto>();
    
    public JavaCodeSnippet(String producto) {
    	this.producto = producto;
    }
    
    //Funci�n que realizar la conexi�n con la API de Amazon y devuelve el archivo XML que se consigue
    //al realizar la conexi�n.
    private String consultaAmazon() throws ParserConfigurationException, SAXException, IOException {

        /*
         * Set up the signed requests helper.
         */
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        String requestUrl = null;

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAJ2M7Z62GFWKITXBA");
        params.put("AssociateTag", "ikor0b-21");
        params.put("SearchIndex", "All");
        params.put("Keywords", producto);
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");

        requestUrl = helper.sign(params);
       
        return requestUrl;
    }    

    public ArrayList<Producto> getListaProductos(){
    	return this.listaProductos;
    }
    
    //Funci�n encargada de conseguir la informaci�n que necesitamos para mostrar los productos.
    public void buscarAmazon() throws IOException, ParserConfigurationException, SAXException {    	 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        String requestUrl = consultaAmazon();
        
        //La funci�n parse devuelve un documento DOM correspondiente al documento XML que recibe como 
        //par�metro. 
        Document doc = db.parse(requestUrl);
        
        //Del nuevo documento DOM se extraen los elementos que tienen como etiqueta 'Item'.
        //Esto se hace as� porque, al examinar el XML que devuelve Amazon, se observa que cada producto
        //est� encerrado entre una �nica etiqueta 'Item'.
        NodeList nList = doc.getElementsByTagName("Item");
        
        System.out.println("Comprobando entradas de: Amazon");
        
        //Por cada �tem, producto, del XML se realiza una iteraci�n.
        for (int i = 0; i < nList.getLength(); i++) {
        	  
           //Inspeccionando el archivo XML, se observa que los datos que queremos extraer est�n dentro de estas etiquetas.
           //La URL est� dentro de la etiqueta DetailPageURL.
           //El nombre del producto dentro de la etiqueta Title.
           //El precio dentro de la etiqueta Price.
           //Por �ltimo, la imagen dentro de la etiqueta ImageSets.
    	   NodeList nodeLink = doc.getElementsByTagName("DetailPageURL");
    	   NodeList nodeTitulo = doc.getElementsByTagName("Title");
    	   NodeList nodePrecio = doc.getElementsByTagName("Price");
    	   NodeList nodeUrlImagen = doc.getElementsByTagName("ImageSets");
			
    	   //La funci�n getTextContent() convierte en texto lo que haya dentro de la etiqueta.
    	   //Por tanto, en estos dos casos, convierte en texto la URL y el nombre del producto.
    	   String link = nodeLink.item(i).getTextContent();
    	   String titulo = nodeTitulo.item(i).getTextContent();
    	   
    	   //El precio es un caso especial. En el archivo XML, lo que hay dentro de la etiqueta Price es:
           //<Price>
           //	<Amount>112</Amount>
           //	<CurrencyCode>EUR</CurrencyCode>
           //	<FormattedPrice>EUR 1,12</FormattedPrice>
           //</Price>
    	   //Nosotros queremos la etiqueta Amount al tener solo que dividirlo entre 100 para poder conseguir
    	   //el precio del producto y ser m�s c�modo para nosotros el convertirlo a un formato correcto para, posteriormente, ordenar
    	   //los precios de forma ascendente.
    	   //La funci�n getChildNodes, en este caso, devolver�a las etiquetas Amount, CurrencyCode y FormattedPrice.
    	   //Al interesarnos Amount, ser�a el item 0 dentro de Price. Y se convierte en texto el amount.
    	   String numero = nodePrecio.item(i).getChildNodes().item(0).getTextContent();
    	   Float nFloat = Float.parseFloat(numero) / (float)100.0;
    	   String precio = Float.toString(nFloat);
    	   
    	   //La URL de la imagen es otro caso especial ya que las im�genes de un mismo producto aparecen en varios sitios.
    	   //El c�digo XML asociado a un ImageSets es el siguiente:
           //<ImageSets>
	       //    <ImageSet Category="variant">
	       //        <SwatchImage>...</SwatchImage>
	       //        <SmallImage>...</SmallImage>
	       //        <ThumbnailImage>...</ThumbnailImage>
	       //        <TinyImage>...</TinyImage>
	       //        <MediumImage>...</MediumImage>
	       //        <LargeImage>
		   //        	<URL>https://images-eu.ssl-images-amazon.com/images/I/51PbF6f3r-L.jpg</URL>
		   //        	<Height Units="pixels">404</Height>
		   //        	<Width Units="pixels">500</Width>
    	   //		 </LargeImage>
	       //    </ImageSet>
	       //    <ImageSet Category="variant"></ImageSet>
           //</ImageSets>
    	   //
    	   //Como se puede observar, dentro de la etiqueta 'ImageSets' hay dos ImageSet.
    	   //A su vez, dentro de un ImageSet, hay distintas etiquetas que se corresponden con distintos tama�os de im�genes.
    	   //Al querer una im�gen grande, tenemos que acceder a la etiqueta 'LargeImage' y, por �ltimo, a la etiqueta 'URL'.
    	   //Lo anterior descrito ser�a la siguiente l�nea de c�digo:
    	   
    	   String urlImagen = nodeUrlImagen.item(i).getChildNodes().item(0).getChildNodes().item(5).getChildNodes().item(0).getTextContent();
    
    	   Producto p = new Producto(precio, titulo, link, urlImagen);
    	   listaProductos.add(p);
        }
    }
}