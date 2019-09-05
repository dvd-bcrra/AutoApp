/*
***URBANTAPP***
Sistema de simulación de transporte en tiempo real
Autor: becerra
Fecha: 04/09/2019
*/

package autoapp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import org.codehaus.jackson.map.ObjectMapper;

public class AutoApp {

    static Route[] routes;
    
    public static void main(String[] args) throws IOException {
        LoadDriver();
        LoadRoutes();
        
        GPS_Simulator myGPS = new GPS_Simulator(routes);
        myGPS.run();
        
        Listener_Simulator myListener = new Listener_Simulator();
        myListener.run();
    }
    
    //Cargar el driver de MySQL
    private static void LoadDriver(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
    }
    
    private static void LoadRoutes() throws FileNotFoundException, IOException{
        ObjectMapper om = new ObjectMapper();
        File file = new File("ruta.json");
        Route[] lr = om.readValue(file, Route[].class);
        routes = lr;
    }
}

class GPS_Simulator extends Thread{
    
    Connection conn;
    Route[] rutas;
    
    public GPS_Simulator(Route[] rutas){
        CrearConexion();
        this.rutas = rutas;
    }
    
    @Override
    public void run(){
        AutoUpdate(0);
    }
    
    public void AutoUpdate(int init_i){
        Timer t = new Timer();
        
        TimerTask tt;
        tt = new TimerTask() {
            int index = init_i;         //indice
            @Override
            public void run() {
                double lat,lng;
                
                if(index == rutas.length-1){
                    index = 0;
                }
                
                lat = rutas[index].getLat();
                lng = rutas[index].getLng();
                index++;

                String sql = "UPDATE Rutas set ubicacion=ST_GeomFromText('POINT(" + lat + " " + lng + ")', 4326) where idRuta = 4";

                try {
                    Statement command = conn.createStatement();
                    command.executeUpdate(sql);
                } catch (SQLException ex) {
                    Logger.getLogger(GPS_Simulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }; 
        
        t.schedule(tt, 0,5000);
    }
    
    //USESE EN CASO DE EMERGENCIA
    public void DeleteAll(){
        String sql = "DELETE FROM Rutas;";
        try {
            Statement command = conn.createStatement();
            command.executeUpdate(sql);
            System.out.println("Se han limpiado las ubicaciones");
        } catch (SQLException ex) {
            Logger.getLogger(GPS_Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void CrearConexion(){
        try{
            conn = DriverManager.getConnection("jdbc:mysql://sql33.main-hosting.eu/u573976610_trans","u573976610_abner","quimica123");
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
    }
}

class Listener_Simulator extends Thread{
    
    Connection conn;
    
    public Listener_Simulator(){
        CrearConexion();
    }
    
    @Override
    public void run(){
        AutoSelect();
    }
    
    public void AutoSelect(){
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                String sql = "SELECT ST_X(t.ubicacion) as x_coor, ST_Y(t.ubicacion) as y_coor, t.*"+
                        "FROM Rutas t WHERE idRuta = 4";
                try {
                    Statement command = conn.createStatement();
                    ResultSet rs = command.executeQuery(sql);
                    if(rs.next()){
                        System.out.println("lat: " + rs.getDouble("x_coor") + ", lng: " + rs.getDouble("y_coor"));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Listener_Simulator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
        t.schedule(tt, 5000, 5000);
    }
    
    private void CrearConexion(){
        try{
            conn = DriverManager.getConnection("jdbc:mysql://sql33.main-hosting.eu/u573976610_trans","u573976610_abner","quimica123");
        }catch(Exception ex){
            System.out.println(ex.toString());
        }
    }
}

class Route
{
    public double getLat() { 
         return this.lat;
    }
    public double setLat(double lat) { 
         return this.lat = lat;
    }
    double lat;

    public double getLng() { 
         return this.lng;
    }
    public double setLng(double lng) { 
         return this.lng = lng;
    }
    double lng;

}