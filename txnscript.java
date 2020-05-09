
import java.util.Date;

// import libinsa.txnscriptUtil ;
// import libinsa.insaLogger ;

// types retournés par les opérations JDBC
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
// import java.sql.ResultSet;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class txnscript
{
	
	private static String jdbcHerokuMachine = "ec2-46-137-156-205.eu-west-1.compute.amazonaws.com" ;
	private static String jdbcHerokuDatabase = "d563fbr8c46bl0" ;
	private static String jdbcHerokuUser = "wykydzvqoqlqfs" ;
	private static String jdbcHerokuPass = "24ec2cac06e27270912bea3427ffb3aae3e515dad854af730cc383220371b100" ;
	
	
// exemple MYSQL LOCAL
	private static String jdbcMysqlMachine = "localhost" ;
	private static String jdbcMysqlDatabase = "exb1610" ;
	private static String jdbcMysqlUser = "root" ;
	private static String jdbcMysqlPass = "tsimiski4" ;
	private static String jdbcMysqlIntricacies = "zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=UTC" ;
	
	private static String jdbcUrl ;
	
	private static String jdbcMachine = "" ;
	private static String jdbcDatabase = "" ;
	private static String jdbcUser = "" ;
	private static String jdbcPass = "" ;	
	
	static Connection cnx = null ;
	static Statement stmt = null ;
	static PreparedStatement pstmt = null ;
	static ResultSet resultSet = null ;
	// private static insaLogger logger = insaLogger.getLogger(txnscript.class);
	private static String saut_de_ligne = "\n" ;
	
    private txnscript()
    {
		// identifier le pilote charge
		boolean bCheckMySQL = checkMySQL () ;
		if ( bCheckMySQL == true )
		{
			try
			{
				jdbcMachine = jdbcMysqlMachine ;
				jdbcPass = jdbcMysqlPass ;
				jdbcDatabase = jdbcMysqlDatabase ;
				jdbcUser = jdbcMysqlUser ;

				jdbcUrl = "jdbc:mysql://" + jdbcMachine + ":3306/" + jdbcDatabase ;
			
				// https://downloads.mysql.com/docs/connector-j-8.0-en.a4.pdf
				// https://www.developpez.net/forums/d1876029/java/general-java/server-time-zone-non-reconnu/
				jdbcUrl = jdbcUrl + "?user=" + jdbcUser + "&password=" + jdbcPass + "&" + jdbcMysqlIntricacies ;
				
				// jdbcUrl = "jdbc:mysql://root:tsimiski4@localhost:3306/exb1610" ;
				cnx = DriverManager.getConnection(jdbcUrl);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				// logger.error ( "JDBC_DATABASE_URL : " + jdbcUrl ) ;
				System.out.println( "txnscript - Probleme de connection MySQL : " + jdbcUrl + " user = " + jdbcUser + " - pass = " + jdbcPass  );
			}   
		}
		
		boolean bCheckPostgreSQL = checkPostgreSQL () ;
		if ( bCheckPostgreSQL == true )
		{
			jdbcUrl = System.getenv("JDBC_DATABASE_URL");
			
			if ( jdbcUrl == null )
			{
				// execution de simplissimeCmdline hors de HEROKU (local ou Github Actions)
				
				jdbcMachine = jdbcHerokuMachine ;
				jdbcPass = jdbcHerokuPass ;
				jdbcDatabase = jdbcHerokuDatabase ;
				jdbcUser = jdbcHerokuUser ;
				jdbcUrl = "jdbc:postgresql://" + jdbcMachine + ":5432/" + jdbcDatabase + "?user=" + jdbcUser + "&password=" + jdbcPass + "&sslmode=require" ;
			}
			
			try
			{
				cnx = DriverManager.getConnection(jdbcUrl);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				// logger.error ( "JDBC_DATABASE_URL : " + jdbcUrl ) ;
				System.out.println( "txnscript - Probleme de connection PostgreSQL : " + jdbcUrl );
			}   
		}
    }   

	public static String getJdbcUrl ()
	{
		return jdbcUrl ;
	}

    public static txnscript getTxnscript() {
        return new txnscript();
    }



    public static boolean checkPostgreSQL()
    {
		try
		{
				Class.forName("org.postgresql.Driver");
			return true ;
		}
		catch (ClassNotFoundException e)
		{
			// System.out.println( "\n\nLa bibliotheque PostgreSQL est absente !");
			// e.printStackTrace();
			return false ;
		}
		catch (Exception e) {
			System.out.println( "\nException\n" ) ;
			e.printStackTrace();
			return false ;
		}
	}


	
    public static boolean checkMySQL()
    {
		// mySQL library
		// https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-usagenotes-connect-drivermanager.html
		try
		{
				Class.forName("com.mysql.cj.jdbc.Driver");
				return true ;
		}
		catch (ClassNotFoundException e)
		{
			// System.out.println( "\n\nClassNotFoundException\n" ) ;
			// e.printStackTrace();
			return false ;
		}
		catch (Exception e) {
			System.out.println( "\nException\n" ) ;
			e.printStackTrace();
			return false ;
		}
	}
	
    public static boolean check()
    {
        boolean result = false ;
		StringBuilder out = new StringBuilder() ;
		
		// https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html#package.description
        // auto java.sql.Driver discovery -- no longer need to load a java.sql.Driver class via Class.forName

        // register JDBC driver, optional since java 1.6
		if ( jdbcUrl.isEmpty() )
		{
			out.append("<p>Le mot de passe pour accéder à la base est vide. Connectez-vous à https://jdbc4uemf.herokuapp.com/hello?pass=xxx pour avoir les 4 paramètres de connexion de VOTRE base de données.</p>" ) ;
		}
		
		// vérifier dispo d'une bibliotheque mettant en oeuvre l'interface JDBC
		//  postgreSQL library



		boolean bCheckMySQL = checkMySQL () ;
		if ( bCheckMySQL == false )
		{
			System.out.println( "Le pilote MySQL est absent !");
		}
		else
		{
			System.out.println( "Test connection avec pilote MySQL!");
		}
			
		boolean bCheckPostgreSQL = checkPostgreSQL () ;
		if ( bCheckPostgreSQL == false )
		{
			System.out.println( "Le pilote PostgreSQL est absent !");
		}
		else
		{
			System.out.println( "Test connection avec pilote PostgreSQL.");
		}
			
	

					
		try
		{
			Connection conn = DriverManager.getConnection( jdbcUrl );
			if (conn != null)
			{
				out.append("<p>Connected to the database!</p>\n");
				result = true ;				
			} else {
				out.append("<p>Failed to make connection!</p>");
			}
		} catch (SQLException e) {
			System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return result ;
    }

	
    public static String insertVille (String nom, Integer codePostal)
    {
        String result = "" ;
		
		String sql = "INSERT INTO Villes (nom, code_postal) VALUES(?,?)" ;

		try
		{
				PreparedStatement pstmt = cnx.prepareStatement(sql) ;
				pstmt.setString(1, nom);
				pstmt.setDouble(2, codePostal);
				pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			System.out.println(e.getMessage());
		}
			
		result = result + codePostal ;
		result = result + "/" + nom ;
		result = result + saut_de_ligne ;		
		return result ;
    }
	
    public static String list()
    {
		String result = "" ;
			
		String sql = "SELECT * FROM Villes";

		try
		{
			PreparedStatement preparedStatement = cnx.prepareStatement(sql) ;
			resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				Integer id = resultSet.getInt("id");
				result = result + "/" + id ;
				Integer codePostal = resultSet.getInt("code_postal");
				result = result + "/" + codePostal ;
				String name = resultSet.getString("nom");
				result = result + "/" + name ;
				result = result + saut_de_ligne ;
				// Timestamp createdDate = resultSet.getTimestamp("CREATED_DATE");
			}
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
		return result ;
    }


    public static String searchByCodePostal(Integer searchedCodePostal)
    {
		String result = "" ;
			
		String sql = "SELECT * FROM Villes WHERE code_postal = ?" ;

		try
		{
			PreparedStatement preparedStatement = cnx.prepareStatement(sql) ;
			preparedStatement.setInt(1, searchedCodePostal);
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next())
			{
				Integer id = resultSet.getInt("id");
				result = result + "/" + id ;
				Integer codePostal = resultSet.getInt("code_postal");
				result = result + "/" + codePostal ;
				String name = resultSet.getString("nom");
				result = result + "/" + name ;
				result = result + saut_de_ligne ;
			}
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
	    
		return result ;
    }

	
    // fonction existante refactorée pour EXB1613
    public static String updateVille (Integer id, String nom, Integer codePostal)
    {
        String result = "" ;
		
		if ( id == null )
		{
			result = updateVilleByName ( nom, codePostal ) ;
		}
		else
		{
			String sql = "UPDATE Villes SET nom = ?, code_postal = ? WHERE id = ?" ;

			try
			{
					PreparedStatement pstmt = cnx.prepareStatement(sql) ;
					pstmt.setString(1, nom);
					pstmt.setDouble(2, codePostal);
					pstmt.setInt(3, id);
					pstmt.executeUpdate();
			}
			catch (SQLException e)
			{
				System.out.println(e.getMessage());
			}
		}
			
		result = result + id ;
		result = result + "/" + codePostal ;
		result = result + "/" + nom ;
		result = result + saut_de_ligne ;		
		return result ;
    }
	


    // creation fonction pour EXB1613
    public static String updateVilleByName (String nom, Integer codePostal)
    {
        String result = "" ;
		String sql = "UPDATE Villes SET code_postal = ? WHERE nom = ?" ;

		try
		{
				PreparedStatement pstmt = cnx.prepareStatement(sql) ;
				pstmt.setDouble(1, codePostal);
				pstmt.setString(2, nom);
				pstmt.executeUpdate();
		}
		catch (SQLException e)
		{
			System.out.println(e.getMessage());
		}
		
		return result ;
    }

	

	
	
    public static String close()
    {
		String result = "" ;
		boolean isClotureOK = true ;
	
		try
		{
			if(resultSet!=null)
			{
				resultSet.close();
				resultSet=null;
			}
		}
		catch (SQLException e)
		{
			isClotureOK = false ;
			e.printStackTrace();
		}
		
		try
		{
			if(stmt!=null)
			{
				stmt.close();
				stmt=null;
			}
		}
		catch (SQLException e)
		{
			isClotureOK = false ;
			e.printStackTrace();
		}
		
		try
		{
			if(pstmt!=null)
			{
				pstmt.close();
				pstmt=null;
			}
		}
		catch (SQLException e)
		{
			isClotureOK = false ;
			e.printStackTrace();
		}

		try
		{
			if(cnx!=null)
			{
				cnx.close();
				cnx=null;
			}
		}
		catch (SQLException e)
		{
			isClotureOK = false ;
			e.printStackTrace();
		}

		if ( isClotureOK == true )
		{
			result = "Cloture objets JDBC OK" ;
		}
		else
		{
			result = "Cloture objets JDBC KO" ;
		}

		return result ;
	}
}
© 2020 GitHub, Inc.
Terms
Privacy
Security
Status
Help
Contact GitHub
Pricing
API
Training
Blog
About
