/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 */
public class Cafe {

   // Reference to physical database connection.
   private Connection _connection = null;

   // Handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end Cafe

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i) {
            System.out.print(rs.getString(i) + "\t");
         }
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      // int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      // boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and counts the number of results.
      while (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the next value from a sequence. This
    * method issues the query to the DBMS and returns the next
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return next value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getNextSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select nextval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /*
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     Cafe.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      Cafe esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorizedUser = null;
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorizedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice, try again.");
                  break;
            }// end switch
            if (authorizedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("\nMAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. Go to Menu");
                  System.out.println("2. Update Profile");
                  System.out.println("3. Place an Order");
                  System.out.println("4. Update an Order");
                  System.out.println(".........................");
                  System.out.println("9. Log Out");
                  switch (readChoice()) {
                     case 1:
                        Menu(esql, authorizedUser);
                        break;
                     case 2:
                        UpdateProfile(esql, authorizedUser);
                        break;
                     case 3:
                        PlaceOrder(esql, authorizedUser);
                        break;
                     case 4:
                        UpdateOrder(esql, authorizedUser);
                        break;
                     case 9:
                        System.out.println("\nSuccessfully logged out.");
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
            // FIX: ELSE STATEMENT for authorizedUser == null (Print wrong user or password)
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("\nDisconnecting from the database... ");
               esql.cleanup();
               System.out.println("Done!\n\nBye!");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   /*
    * Prints a greeting for the UI.
    */
   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    */
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Checks if the user is exiting the menu.
    */
   public static boolean checkExit(String input) {
      // Handle quitting
      if (input.equalsIgnoreCase("DONE")  || input.equalsIgnoreCase("QUIT") || input.equalsIgnoreCase("Q")) {
         return false;
      }
      return true;
   }

   /*
    * Checks passwords for num chars, special chars etc.
    */
   public static boolean checkPassword(String password) {
      boolean hasCapital = false;
      boolean hasSpecial = false;
      boolean hasEightNums = false;
      for (int i = 0; i < password.length(); ++i) {
         char currentChar = password.charAt(i);
         if (Character.isUpperCase(currentChar)) {
            hasCapital = true;
         }
         if (!(Character.isDigit(currentChar) || Character.isLetter(currentChar) || Character.isWhitespace(currentChar))) {
            hasSpecial = true;
         }
      }
      if (password.length() >= 8) {
         hasEightNums = true;
      }
      return (hasCapital && hasSpecial && hasEightNums);
   }

   /*
    * Creates a new user with provided login, passowrd and phoneNum
    */
   public static void CreateUser(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();

         // (EC) Add check if the login is already taken
         boolean alreadyExists = false;
         String query = String.format("SELECT * FROM Users U WHERE U.login = '%s'", login);
         int numLogin = esql.executeQuery(query);
         if (numLogin > 0) {
            alreadyExists = true;
         }
         if (alreadyExists) {
            System.out.println("Username already taken please try again.");
         }
         else {
            System.out.print("\tEnter user password: ");
            String password = in.readLine();
            // (EC) Add a check if the password does not contain certain characters
            if (checkPassword(password)) {
               System.out.print("\tEnter user phone: ");
               String phone = in.readLine();

               String type = "Customer";
               String favItems = "";
               String query2 = String.format(
                     "INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone,
                     login, password, favItems, type);

               esql.executeUpdate(query2);
               System.out.println("User successfully created!");
            }
            else {
               System.out.println("Password must have a minimum of 8 characters including a capital letter and a special character (ex: ~!@#$%^&*_-+=`|(){}[]:;'<>,.?)");
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }// end CreateUser

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    */
   public static String LogIn(Cafe esql) {
      try {
         System.out.print("\nEnter user login: ");
         String login = in.readLine();
         System.out.print("Enter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            String welcome = String.format("\nLogin successful. Welcome, %s!", login);
            System.out.println(welcome);
            return login;
         } else {
            System.out.println("\nLogin not found! Please try again.");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }// end

   /*
    * Users can browse menu and search by name/category.
    * Managers can also update menu items.
    */
   public static void Menu(Cafe esql, String authorizedUser) {
      try {
         // Check if the user is a Manager
         boolean isManager = false;
         String query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager'", authorizedUser);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            isManager = true;

         System.out.println("\nMENU OPTIONS");
         System.out.println("---------");
         System.out.println("1. Browse Menu");
         System.out.println("2. Search By Name");
         System.out.println("3. Search By Category");
         if (isManager) System.out.println("4. Update Menu");
         System.out.println(".........................");
         System.out.println("9. Return to Main Menu");
         int rowNum = 0;

         switch (readChoice()) {

            case 1:
               System.out.println("\nDrinks:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));

               System.out.println("\nSweets:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));

               System.out.println("\nSoup:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));
               break;

            case 2:
               System.out.print("\nEnter item name: ");
               String itemName = in.readLine();
               System.out.println();
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemName);
               rowNum = esql.executeQueryAndPrintResult(query);
               if (rowNum > 0) {
                  System.out.println(String.format("(%d items)", rowNum));
               } else {
                  System.out.println("Item not found, please try again.");
               }
               break;

            case 3:
               System.out.print("\nEnter 'Drinks', 'Sweets', or 'Soup': ");
               String type = in.readLine();
               System.out.println();
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = '%s'", type);
               rowNum = esql.executeQueryAndPrintResult(query);
               if (rowNum > 0) {
                  System.out.println(String.format("(%d items)", rowNum));
               } else {
                  System.out.println("Invalid input, please try again.");
               }
               break;

            case 4:
               if (!isManager) {
                  System.out.println("Unrecognized choice!");
                  break;
               }

               System.out.println("\nEDIT MENU");
               System.out.println("---------");
               System.out.println("1. Add Item");
               System.out.println("2. Delete Item");
               System.out.println("3. Update Item");
               System.out.println(".........................");
               System.out.println("9. Return to Main Menu");

               switch (readChoice()) {

                  case 1:
                     System.out.println("\nEnter the name of the new item: ");
                     String newName = in.readLine();
                     String newNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", newName);
                     rowNum = esql.executeQuery(newNameQuery);
                     while ((newName.length() > 50) || (rowNum > 0)) {
                        System.out.println("Name already exists, or is > 50 characters. Try again: ");
                        newName = in.readLine();
                        newNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", newName);
                        rowNum = esql.executeQuery(newNameQuery);
                     }

                     System.out.println("Enter the item's type ('Drinks', 'Sweets', or 'Soup'): ");
                     String newType = in.readLine();
                     while (!(newType.equals("Drinks")) && !(newType.equals("Sweets")) && !(newType.equals("Soup"))) {
                        System.out.println("Type must be 'Drinks', 'Sweets', or 'Soup'. Try again: ");
                        newType = in.readLine();
                     }

                     System.out.println("Enter the item's price (exclude '$'): ");
                     String newPrice = in.readLine();
                     Float newPriceFloat;
                     try {
                        newPriceFloat = Float.parseFloat(newPrice);  // Parse string as a float
                     } catch (Exception e) {
                        System.out.println("Price must be of the form '12.34'. Please re-add the item.");
                        break;
                     }

                     System.out.println("Enter the item's description: ");
                     String newDescription = in.readLine();
                     while (newDescription.length() > 400) {
                        System.out.println("Description must be less than 400 characters. Try again: ");
                        newDescription = in.readLine();
                     }

                     System.out.println("Enter the item's image URL: ");
                     String newImageURL = in.readLine();
                     while (newImageURL.length() > 256) {
                        System.out.println("Image URL must be less than 256 characters. Try again: ");
                        newImageURL = in.readLine();
                     }

                     String newItemUpdate = String.format("INSERT INTO Menu VALUES ('%s', '%s', '%s', '%s', '%s')", newName, newType, newPriceFloat.toString(), newDescription, newImageURL);
                     esql.executeUpdate(newItemUpdate);
                     System.out.println("\nItem added!");
                     break;

                  case 2:
                     System.out.println("\nEnter the name of the item to delete: ");
                     String deleteName = in.readLine();
                     String deleteNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", deleteName);
                     rowNum = esql.executeQuery(deleteNameQuery);
                     while ((deleteName.length() > 50) || (rowNum == 0)) {
                        System.out.println("Name not found, or is > 50 characters. Try again: ");
                        deleteName = in.readLine();
                        deleteNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", deleteName);
                        rowNum = esql.executeQuery(deleteNameQuery);
                     }
                     String deletionUpdate = String.format("DELETE FROM Menu WHERE itemName = '%s'", deleteName);
                     esql.executeUpdate(deletionUpdate);
                     System.out.println("\nItem deleted!");
                     break;

                  case 3:
                     System.out.println("\nEnter the name of the item to update: ");
                     String itemToUpdate = in.readLine();
                     String itemToUpdateQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", itemToUpdate);
                     rowNum = esql.executeQuery(itemToUpdateQuery);
                     while ((itemToUpdate.length() > 50) || (rowNum == 0)) {
                        System.out.println("Item doesn't exist, or is > 50 characters. Try again: ");
                        itemToUpdate = in.readLine();
                        itemToUpdateQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", itemToUpdate);
                        rowNum = esql.executeQuery(itemToUpdateQuery);
                     }

                     System.out.println(String.format("\nUPDATE ITEM MENU (updating '%s')", itemToUpdate));
                     System.out.println("---------");
                     System.out.println("1. Update Name");
                     System.out.println("2. Update Type");
                     System.out.println("3. Update Price");
                     System.out.println("4. Update Description");
                     System.out.println("5. Update Image URL");
                     System.out.println(".........................");
                     System.out.println("9. Return to Main Menu");

                     switch (readChoice()) {

                        case 1:
                           System.out.println(String.format("Enter a new name for '%s': ", itemToUpdate));
                           String newItemName = in.readLine();
                           String newItemNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", newItemName);
                           rowNum = esql.executeQuery(newItemNameQuery);
                           while ((newItemName.length() > 50) || (rowNum > 0)) {
                              System.out.println("Name already exists, or is > 50 characters. Try again: ");
                              newItemName = in.readLine();
                              newItemNameQuery = String.format("SELECT * FROM Menu WHERE itemName = '%s'", newItemName);
                              rowNum = esql.executeQuery(newItemNameQuery);
                           }
                           String itemNameUpdate = String.format("UPDATE Menu SET itemName = '%s' WHERE itemName = '%s'", newItemName, itemToUpdate);
                           esql.executeUpdate(itemNameUpdate);
                           System.out.println("\nName updated!");
                           break;

                        case 2:
                           System.out.println(String.format("Enter a new type for '%s' ('Drinks', 'Sweets', or 'Soup'): ", itemToUpdate));
                           String newItemType = in.readLine();
                           while (!(newItemType.equals("Drinks")) && !(newItemType.equals("Sweets")) && !(newItemType.equals("Soup"))) {
                              System.out.println("Type must be 'Drinks', 'Sweets', or 'Soup'. Try again: ");
                              newItemType = in.readLine();
                           }
                           String itemTypeUpdate = String.format("UPDATE Menu SET type = '%s' WHERE itemName = '%s'", newItemType, itemToUpdate);
                           esql.executeUpdate(itemTypeUpdate);
                           System.out.println("\nType updated!");
                           break;

                        case 3:
                           System.out.println(String.format("Enter a new price for '%s' (of the form 12.34): ", itemToUpdate));
                           String newItemPrice = in.readLine();
                           Float newItemPriceFloat;
                           try {
                              newItemPriceFloat = Float.parseFloat(newItemPrice);  // Parse string as a float
                           } catch (Exception e) {
                              System.out.println("Price must be of the form '12.34'. Please restart the update.");
                              break;
                           }
                           String itemPriceUpdate = String.format("UPDATE Menu SET price = '%s' WHERE itemName = '%s'", newItemPriceFloat.toString(), itemToUpdate);
                           esql.executeUpdate(itemPriceUpdate);
                           System.out.println("\nPrice updated!");
                           break;

                        case 4:
                           System.out.println(String.format("Enter a new description for '%s': ", itemToUpdate));
                           String newItemDescription = in.readLine();
                           while (newItemDescription.length() > 400) {
                              System.out.println("Description must be less than 400 characters. Try again: ");
                              newItemDescription = in.readLine();
                           }
                           String itemDescriptionUpdate = String.format("UPDATE Menu SET description = '%s' WHERE itemName = '%s'", newItemDescription, itemToUpdate);
                           esql.executeUpdate(itemDescriptionUpdate);
                           System.out.println("\nDescription updated!");
                           break;

                        case 5:
                           System.out.println(String.format("Enter a new image URL for '%s': ", itemToUpdate));
                           String newItemImageURL = in.readLine();
                           while (newItemImageURL.length() > 256) {
                              System.out.println("Image URL must be less than 256 characters. Try again: ");
                              newItemImageURL = in.readLine();
                           }
                           String itemImageURLUpdate = String.format("UPDATE Menu SET imageURL = '%s' WHERE itemName = '%s'", newItemImageURL, itemToUpdate);
                           esql.executeUpdate(itemImageURLUpdate);
                           System.out.println("\nImage URL updated!");
                           break;

                        default:
                           System.out.println("Unrecognized choice!");
                           break;
                     }

                     break;

                  default:
                     System.out.println("Unrecognized choice!");
                     break;
               }
               break;

            case 9:
               break;
            default:
               System.out.println("Unrecognized choice!");
               break;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Users can update their own profile information.
    * Managers can update the profiles of other users.
    */
   public static void UpdateProfile(Cafe esql, String authorizedUser) {
      try {
         boolean isManager = false;
         String query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager'", authorizedUser);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            isManager = true;
         }

         String updatedUser = authorizedUser;
         List<List<String>> queryResults = new ArrayList<List<String>>();
         queryResults = esql.executeQueryAndReturnResult(String.format("SELECT * FROM Users WHERE login = '%s'", authorizedUser));
         String userPassword = queryResults.get(0).get(2);
         String userPhone = queryResults.get(0).get(1);
         String userFavItems = queryResults.get(0).get(3);

         System.out.println(String.format("\nUPDATE PROFILE OPTIONS (user: %s)", updatedUser));
         System.out.println("----------------------");
         System.out.println(String.format("Password: %s", userPassword));
         System.out.println(String.format("Phone #: %s", userPhone));
         System.out.println(String.format("Fav Items: %s", userFavItems).trim());
         System.out.println("----------------------");
         System.out.println("1. Change Password");
         System.out.println("2. Change Phone Number");
         System.out.println("3. Change Favorite Items");
         if (isManager) System.out.println("4. Update a Different User");
         System.out.println(".........................");
         System.out.println("9. Return to Main Menu");
         int rowNum = 0;

         switch (readChoice()) {

            case 1:
               System.out.println("\nEnter a new password: ");
               String password = in.readLine();
               while (!checkPassword(password)) {
                  System.out.println("Password must have a minimum of 8 characters including a capital letter and a special character (ex: ~!@#$%^&*_-+=`|(){}[]:;'<>,.?)");
                  password = in.readLine();
               }
               query = String.format("UPDATE USERS SET password = '%s' WHERE login = '%s'", password, updatedUser);
               esql.executeUpdate(query);
               System.out.println("User successfully created!");
               System.out.println("\nYour password has been updated.");
               break;

            case 2:
               System.out.println("\nEnter a new phone number: ");
               String phoneNum = in.readLine().trim();
               String newPhoneQuery = String.format("SELECT * FROM Users WHERE phoneNum = '%s'", phoneNum);
               rowNum = esql.executeQuery(newPhoneQuery);
               while ((rowNum > 0) || (phoneNum.length() > 11)) {
                  System.out.println("Phone number is taken or is too long, please try again: ");
                  phoneNum = in.readLine().trim();
                  newPhoneQuery = String.format("SELECT * FROM Users WHERE phoneNum = '%s'", phoneNum);
                  rowNum = esql.executeQuery(newPhoneQuery);
               }
               query = String.format("UPDATE USERS SET phoneNum = '%s' WHERE login = '%s'", phoneNum, updatedUser);
               esql.executeUpdate(query);
               System.out.println("\nYour phone number has been updated.");
               break;

            case 3:
               System.out.println("\nList of your favorite items: \n");
               query = String.format("SELECT favItems FROM USERS WHERE login = '%s'", updatedUser);
               rowNum = esql.executeQueryAndPrintResult(query);
               if (rowNum > 0) {
                  System.out.println(String.format("(%d items)", rowNum));
               }
               else {
                  System.out.println("ERROR: Issue with finding favorite items. Please try again later.");
               }

               System.out.println("\nPlease enter your new list of favorite items separated by commas.\n");
               String favItems = in.readLine();
               query = String.format("UPDATE USERS SET favItems = '%s' WHERE login = '%s'", favItems, updatedUser);
               esql.executeUpdate(query);
               System.out.println("\nYour list of favorite items has been updated.");
               break;

            case 4:
               if (!isManager) {
                  System.out.println("Unrecognized choice!");
               }
               else {
                  System.out.println("\nPlease enter the username of the User you are changing:");
                  updatedUser = in.readLine();

                  boolean userFound = false;
                  query = String.format("SELECT * FROM Users U WHERE U.login = '%s'", updatedUser);
                  int numLogin = esql.executeQuery(query);
                  if (numLogin > 0) {
                     userFound = true;
                  }
                  while(!userFound && !updatedUser.equals("9")) {
                     System.out.println("Username not found. Please try again or press '9' to quit.");
                     updatedUser = in.readLine();
                     query = String.format("SELECT * FROM Users U WHERE U.login = '%s'", updatedUser);
                     numLogin = esql.executeQuery(query);
                     if (numLogin > 0) {
                        userFound = true;
                     }
                  }
                  if (!updatedUser.equals("9")) {
                     UpdateProfile(esql, updatedUser);
                  }
               }
               break;
            case 9:
               break;
            default:
               System.out.println("Unrecognized choice!");
               break;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Users can place orders by specifying items and comments.
    * Menu, order details and running total is displayed.
    */
   public static void PlaceOrder(Cafe esql, String authorizedUser) {
      try {
         String query;
         int rowNum;
         ArrayList<String> orderItems = new ArrayList<String>();
         ArrayList<Float> orderPrices = new ArrayList<Float>();
         ArrayList<String> orderComments = new ArrayList<String>();
         List<List<String>> queryResults = new ArrayList<List<String>>();

         // Print menu for the user first
         System.out.println("\nDrinks:\n-------------------------");
         query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
         rowNum = esql.executeQueryAndPrintResult(query);
         System.out.println(String.format("(%d items)", rowNum));

         System.out.println("\nSweets:\n-------------------------");
         query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
         rowNum = esql.executeQueryAndPrintResult(query);
         System.out.println(String.format("(%d items)", rowNum));

         System.out.println("\nSoup:\n-------------------------");
         query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
         rowNum = esql.executeQueryAndPrintResult(query);
         System.out.println(String.format("(%d items)", rowNum));

         System.out.println("\nORDER MENU");
         System.out.println("--------------");
         System.out.println("The menu is shown above! Type 'DONE' to finish.");

         int i = 1;
         int itemIndex = 0;
         String itemName = "";
         String itemPrice = "";
         String itemComment = "";
         Float totalPrice = (float)0.0;
         int nextOrderID = 0;

         // Collect items for the order
         while (!itemName.equalsIgnoreCase("DONE")) {
            System.out.print(String.format("\nEnter name for item #%d, or 'DONE' to finish: ", i));
            itemName = in.readLine().trim();

            // Get item name, make sure it's valid
            query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemName);
            queryResults = esql.executeQueryAndReturnResult(query);
            while ((queryResults.size() == 0) && !itemName.equalsIgnoreCase("DONE")) {
               System.out.print("Item not found, try again: ");
               itemName = in.readLine().trim();
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemName);
               queryResults = esql.executeQueryAndReturnResult(query);
            }

            // Make sure item isn't a duplicate
            itemIndex = orderItems.indexOf(itemName);
            while (itemIndex != -1) {
               System.out.print("Item already added, try again: ");
               itemName = in.readLine().trim();
               itemIndex = orderItems.indexOf(itemName);
            }

            // Handle quitting
            if (itemName.equalsIgnoreCase("DONE")) {
               if (orderItems.size() == 0) {
                  System.out.println("\nGoodbye!");
                  return;
               }
               break;
            }

            // Comment for each item
            System.out.print("Enter item comments, or 'None': ");
            itemComment = in.readLine().trim();
            while (itemComment.length() > 130) {
               System.out.print("\nComment must be under 130 chars. Try again: ");
               itemComment = in.readLine().trim();
            }
            if (itemComment.equalsIgnoreCase("None")) {
               itemComment = "";
            }

            itemPrice = queryResults.get(0).get(1);         // Get item price as string
            orderPrices.add(Float.parseFloat(itemPrice));   // Convert price to float, add to list
            orderItems.add(queryResults.get(0).get(0));     // Add item name to list
            orderComments.add(itemComment);                 // Add item comment to list

            System.out.println("\nYour Order: ");
            System.out.println("--------------");
            for (int j = 0; j < orderItems.size(); ++j) {
               System.out.println(String.format("%d) %s ($%.2f)", j+1, orderItems.get(j), orderPrices.get(j)));
            }
            totalPrice = (float)0.0;
            for (int k = 0; k < orderPrices.size(); ++k) {
               totalPrice += orderPrices.get(k);
            }
            System.out.println(String.format("\nTOTAL: $%.2f", totalPrice));

            ++i;
         }

         // Create new Order, first orderid will be 87257
         nextOrderID = esql.getNextSeqVal("orders_orderid_seq");
         query = String.format("INSERT INTO Orders VALUES ('%d', '%s', 'false', 'now()', '%f')",
               nextOrderID, authorizedUser, totalPrice);
         esql.executeUpdate(query);

         // Create ItemStatus for each item
         for (i = 0; i < orderItems.size(); ++i) {
            query = String.format("INSERT INTO ItemStatus VALUES ('%d', '%s', 'now()', 'Hasn''t started', '%s')",
                  nextOrderID, orderItems.get(i), orderComments.get(i));
            esql.executeUpdate(query);
         }

         System.out.println("\nYour order has been placed!");

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   /*
    * Users can view their 5 most recent orders and update them.
    * Managers can update others' orders.
    */
   public static void UpdateOrder(Cafe esql, String authorizedUser) {
      try {
         String query;
         String inputOrderString;
         String itemToUpdate;
         String itemComment;
         Float totalPrice = (float)0.0;
         Float itemPrice = (float)0.0;
         int inputOrderID;
         int numRows;
         boolean wantToChange = true;
         boolean wantToAdd = true;
         boolean wantToDelete = true;
         boolean wantToUpdate = true;
         boolean usermenu = true;
         boolean orderMenu = true;
         boolean isAuthorized = false;
         List<List<String>> queryResults = new ArrayList<List<String>>();

         // Check if user is a manager
         boolean isManager = false;
         query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager'", authorizedUser);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            isManager = true;
            isAuthorized = true;
         }

         // Update Order Menu Options
         while (usermenu) {
            // Print most recent 5 orders from authorizedUser
            System.out.println(String.format("\n%s's 5 Most Recent Orders:\n-------------------------", authorizedUser));
            query = String.format("SELECT * FROM Orders WHERE login = '%s' ORDER BY timestamprecieved DESC LIMIT 5", authorizedUser);
            esql.executeQueryAndPrintResult(query);

            // Output UPDATE ORDER MENU
            System.out.println("\nUPDATE ORDER MENU");
            System.out.println("-----------------");
            System.out.println("1. Edit an Order");
            System.out.println("2. Delete an Order");
            if (isManager) System.out.println("3. View All Orders Within 24 Hours");
            System.out.println(".........................");
            System.out.println("9. Return to MAIN MENU");

            switch (readChoice()) {
               case 1:
                  orderMenu = true;
                  wantToChange = true;
                  System.out.print("\nPlease enter the orderID of the order you are changing: ");
                  inputOrderString = in.readLine();
                  inputOrderID = Integer.parseInt(inputOrderString);

                  // Check if order exists
                  query = String.format("SELECT * FROM Orders WHERE orderid = '%d'", inputOrderID);
                  numRows = esql.executeQuery(query);
                  wantToChange = checkExit(inputOrderString);
                  while (numRows <= 0 && wantToChange) {
                     System.out.println(String.format("Orderid '%d' not found.", inputOrderID));
                     System.out.print("\nPlease re-enter the orderID of the order you are changing or type 'DONE' to exit: ");

                     inputOrderString = in.readLine();
                     inputOrderID = Integer.parseInt(inputOrderString);
                     wantToChange = checkExit(inputOrderString);

                     query = String.format("SELECT * FROM Orders WHERE orderid = '%d'", inputOrderID);
                     numRows = esql.executeQuery(query);
                  }

                  // OrderID Exists
                  if (numRows > 0) {
                     if (!isManager) {
                        // Check if it is their own order
                        query = String.format("SELECT * FROM Orders WHERE login = '%s' AND orderid = '%d'", authorizedUser, inputOrderID);
                        numRows = esql.executeQuery(query);
                        if (numRows <= 0) {
                           System.out.println(String.format("Not authorized to change orderid '%d'.", inputOrderID));
                        }
                        else {
                           // Check if paid for
                           query = String.format("SELECT * FROM Orders WHERE login = '%s' AND orderid = '%d' AND paid = false", authorizedUser, inputOrderID);
                           numRows = esql.executeQuery(query);
                           if (numRows <= 0) {
                              System.out.println(String.format("\nOrderid '%d' already paid for. Cannot change order.", inputOrderID));
                           }
                           else {
                              isAuthorized = true;
                           }
                        }
                     }

                     // The user is authorized to change the order
                     while (isAuthorized && orderMenu) {
                        // SUCCESSFUL ORDERID
                        System.out.println("\nOptions:");
                        System.out.println("---------");
                        System.out.println("1. View Order and Total");
                        System.out.println("2. Add Item");
                        System.out.println("3. Delete Item");
                        System.out.println("4. Update Comment");
                        if (isManager) System.out.println("5. Mark as Paid");
                        System.out.println(".........................");
                        System.out.println("9. Return to UPDATE ORDER MENU");

                        switch (readChoice()) {
                           case 1:
                              // Output current order
                              System.out.println(String.format("\nOrder %d's Itemized List:\n-------------------------", inputOrderID));
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d'", inputOrderID);
                              esql.executeQueryAndPrintResult(query);

                              // Output Total Price
                              query = String.format("SELECT total FROM Orders WHERE orderid = '%d'", inputOrderID);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              totalPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                              System.out.println(String.format("Total price: $%.2f\n", totalPrice));

                              break;
                           case 2:
                              wantToAdd = true;
                              // Output current order
                              System.out.println(String.format("\nOrder %d's Itemized List:\n-------------------------", inputOrderID));
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d'", inputOrderID);
                              esql.executeQueryAndPrintResult(query);


                              // Ask for itemName
                              System.out.print("Please type the item name you would like to add or type 'M' for the menu: ");
                              itemToUpdate = in.readLine();
                              wantToAdd = checkExit(itemToUpdate);

                              if (itemToUpdate.equals("M") || itemToUpdate.equals("m")) {
                                 // Output Menu Items
                                 System.out.println(String.format("\nMenu Items:\n-------------------------", inputOrderID));
                                 System.out.println("\nDrinks:\n-------------------------");
                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
                                 numRows = esql.executeQueryAndPrintResult(query);
                                 System.out.println(String.format("(%d items)", numRows));

                                 System.out.println("\nSweets:\n-------------------------");
                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
                                 numRows = esql.executeQueryAndPrintResult(query);
                                 System.out.println(String.format("(%d items)", numRows));

                                 System.out.println("\nSoup:\n-------------------------");
                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
                                 numRows = esql.executeQueryAndPrintResult(query);
                                 System.out.println(String.format("(%d items)", numRows));

                                 System.out.print("\nPlease type the item name you would like to add or type 'DONE': ");
                                 itemToUpdate = in.readLine();
                                 wantToAdd = checkExit(itemToUpdate);
                              }

                              // Get item name, make sure it's valid
                              query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              while ((queryResults.size() == 0) && wantToAdd) {
                                 System.out.print("Item not found, try again or type 'DONE': ");
                                 itemToUpdate = in.readLine();
                                 wantToAdd = checkExit(itemToUpdate);

                                 if (itemToUpdate.equals("M") || itemToUpdate.equals("m")) {
                                    // Output Menu Items
                                    System.out.println(String.format("\nMenu Items:\n-------------------------", inputOrderID));
                                    System.out.println("\nDrinks:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.println("\nSweets:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.println("\nSoup:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.print("\nPlease type the item name you would like to add or type 'DONE': ");
                                    itemToUpdate = in.readLine();
                                    wantToAdd = checkExit(itemToUpdate);
                                 }

                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                              }

                              // Make sure item isn't a duplicate
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s'", inputOrderID, itemToUpdate);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              numRows = esql.executeQuery(query);
                              while ((queryResults.size() > 0) && wantToAdd) {
                                 System.out.print("Item already added, try again or type 'DONE': ");
                                 itemToUpdate = in.readLine();
                                 wantToAdd = checkExit(itemToUpdate);

                                 if (itemToUpdate.equals("M") || itemToUpdate.equals("m")) {
                                    // Output Menu Items
                                    System.out.println(String.format("\nMenu Items:\n-------------------------", inputOrderID));
                                    System.out.println("\nDrinks:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.println("\nSweets:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.println("\nSoup:\n-------------------------");
                                    query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
                                    numRows = esql.executeQueryAndPrintResult(query);
                                    System.out.println(String.format("(%d items)", numRows));

                                    System.out.print("\nPlease type the item name you would like to add or type 'DONE': ");
                                    itemToUpdate = in.readLine();
                                    wantToAdd = checkExit(itemToUpdate);
                                 }

                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                              }

                              // Comment for each item
                              if(wantToAdd) {
                                 System.out.print("Enter item comments, or 'None': ");
                                 itemComment = in.readLine();
                                 while (itemComment.length() > 130 && wantToAdd) {
                                    System.out.print("\nComment must be under 130 chars. Try again: ");
                                    itemComment = in.readLine();
                                 }
                                 if (itemComment.equals("None")) {
                                    itemComment = "";
                                 }

                                 query = String.format("INSERT INTO ItemStatus VALUES ('%d', '%s', 'now()', 'Hasn''t started', '%s')",
                                       inputOrderID, itemToUpdate, itemComment);
                                 esql.executeUpdate(query);

                                 query = String.format("SELECT total FROM Orders WHERE orderid = '%d'", inputOrderID);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                                 totalPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                                 // System.out.print(String.format("DEBUG: Total price: %f\n", totalPrice));

                                 query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                                 itemPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                                 // System.out.print(String.format("DEBUG: Item price: %f\n", itemPrice));

                                 query = String.format("UPDATE Orders SET total = '%f' WHERE orderid = '%d'", totalPrice + itemPrice, inputOrderID);
                                 esql.executeUpdate(query);
                              }
                              else {
                                 System.out.println(String.format("Cancelling adding to orderid '%d'...", inputOrderID));
                              }

                              // DEBUG: CHECK TOTAL PRICE AFTERWARDS
                              // query = String.format("SELECT total FROM Orders WHERE orderid = '%d'", inputOrderID);
                              // queryResults = esql.executeQueryAndReturnResult(query);
                              // totalPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                              // System.out.print(String.format("DEBUG: Total price after: %f\n", totalPrice));

                              break;
                           case 3:
                              wantToDelete = true;
                              System.out.println(String.format("\nOrder %d's Itemized List:\n-------------------------", inputOrderID));
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d'", inputOrderID);
                              esql.executeQueryAndPrintResult(query);

                              // Ask for itemName
                              System.out.print("Please type the item name you would like to delete or type 'DONE': ");
                              itemToUpdate = in.readLine();
                              wantToDelete = checkExit(itemToUpdate);

                              // Get item name, make sure it's valid
                              query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              while ((queryResults.size() == 0) && wantToDelete) {
                                 System.out.print("Item not found, try again or type 'DONE': ");
                                 itemToUpdate = in.readLine();
                                 wantToDelete = checkExit(itemToUpdate);

                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                              }

                              // Get item name, make sure it's valid
                              query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              while ((queryResults.size() == 0) && wantToDelete) {
                                 System.out.print("Item not found, try again: ");
                                 itemToUpdate = in.readLine();
                                 wantToDelete = checkExit(itemToUpdate);

                                 query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                              }

                              if (queryResults.size() > 0 && wantToDelete) {
                                 query = String.format("SELECT total FROM Orders WHERE orderid = '%d'", inputOrderID);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                                 totalPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                                 // System.out.print(String.format("DEBUG: Total price: %f\n", totalPrice));

                                 query = String.format("SELECT price FROM Menu WHERE itemName = '%s'", itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                                 itemPrice = Float.valueOf(queryResults.get(0).get(0)).floatValue();         // Get item price as string
                                 // System.out.print(String.format("DEBUG: Item price: %f\n", itemPrice));

                                 query = String.format("DELETE FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s'", inputOrderID, itemToUpdate);
                                 esql.executeUpdate(query);

                                 query = String.format("UPDATE Orders SET total = '%f' WHERE orderid = '%d'", totalPrice - itemPrice, inputOrderID);
                                 esql.executeUpdate(query);
                                 System.out.println("\nItem successfully deleted.");
                              }
                              else {
                                 System.out.println(String.format("Cancelling deleting from orderid '%d'...", inputOrderID));
                              }
                              break;
                           case 4:
                              // Output current order
                              wantToUpdate = true;
                              System.out.println(String.format("\nOrder %d's Itemized List:\n-------------------------", inputOrderID));
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d'", inputOrderID);
                              esql.executeQueryAndPrintResult(query);

                              // Ask for itemName
                              System.out.print("Please type the item name you would like to update or type 'DONE': ");
                              itemToUpdate = in.readLine();
                              wantToUpdate = checkExit(itemToUpdate);

                              // Get item name, make sure it's valid
                              query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s'", inputOrderID, itemToUpdate);
                              queryResults = esql.executeQueryAndReturnResult(query);
                              while ((queryResults.size() == 0) && wantToUpdate) {
                                 System.out.print("Item not found in order, try again or type 'DONE': ");
                                 itemToUpdate = in.readLine();
                                 wantToUpdate = checkExit(itemToUpdate);

                                 query = String.format("SELECT * FROM ItemStatus WHERE orderid = '%d' AND itemName = '%s'", inputOrderID, itemToUpdate);
                                 queryResults = esql.executeQueryAndReturnResult(query);
                              }

                              if ((queryResults.size() > 0) && wantToUpdate) {
                                 System.out.print("Enter item comments, or 'None' (Note: The comment will be replaced.): ");
                                 itemComment = in.readLine();

                                 while (itemComment.length() > 130) {
                                    System.out.print("\nComment must be under 130 chars. Try again: ");
                                    itemComment = in.readLine();
                                 }

                                 if (itemComment.equals("None")) {
                                    itemComment = "";
                                 }

                                 query = String.format("UPDATE ItemStatus SET comments = '%s' WHERE orderid = '%d' AND itemName = '%s'", itemComment, inputOrderID, itemToUpdate);
                                 esql.executeUpdate(query);

                                 System.out.println("\nItem's comment has been updated!");
                              }


                              break;
                           case 5:
                              if (!isManager) {
                                 System.out.println("Unrecognized choice!");
                              }
                              else {
                                 String updatePaidQuery = String.format("UPDATE Orders SET paid = 'true' WHERE orderid = '%d'", inputOrderID);
                                 esql.executeUpdate(updatePaidQuery);
                                 // FIX: change this to be outputing which order was updated
                                 System.out.println(String.format("\nOrderid '%d' marked as paid!", inputOrderID));
                              }
                              break;
                           case 9:
                              orderMenu = false;
                              break;
                           default:
                              System.out.println("Unrecognized choice!");
                           break;
                        }
                     }
                  }
                  break;
               case 2:
                  orderMenu = true;
                  wantToChange = true;
                  System.out.print("\nPlease enter the orderID of the order you are deleting: ");
                  inputOrderString = in.readLine();
                  inputOrderID = Integer.parseInt(inputOrderString);

                  // Check if order exists
                  query = String.format("SELECT * FROM Orders WHERE orderid = '%d'", inputOrderID);
                  numRows = esql.executeQuery(query);
                  wantToChange = checkExit(inputOrderString);
                  while (numRows <= 0 && wantToChange) {
                     System.out.println(String.format("Orderid '%d' not found.", inputOrderID));
                     System.out.print("\nPlease re-enter the orderID of the order you are deleting or type 'DONE' to exit: ");

                     inputOrderString = in.readLine();
                     inputOrderID = Integer.parseInt(inputOrderString);
                     wantToChange = checkExit(inputOrderString);

                     query = String.format("SELECT * FROM Orders WHERE orderid = '%d'", inputOrderID);
                     numRows = esql.executeQuery(query);
                  }

                  // OrderID Exists
                  if (numRows > 0) {
                     if (!isManager) {
                        // Check if it is their own order
                        query = String.format("SELECT * FROM Orders WHERE login = '%s' AND orderid = '%d'", authorizedUser, inputOrderID);
                        numRows = esql.executeQuery(query);
                        if (numRows <= 0) {
                           System.out.println(String.format("Not authorized to change orderid '%d'.", inputOrderID));
                        }
                        else {
                           // Check if paid for
                           query = String.format("SELECT * FROM Orders WHERE login = '%s' AND orderid = '%d' AND paid = false", authorizedUser, inputOrderID);
                           numRows = esql.executeQuery(query);
                           if (numRows <= 0) {
                              System.out.println(String.format("Orderid '%d' already paid for. Cannot change order.", inputOrderID));
                           }
                           else {
                              isAuthorized = true;
                           }
                        }
                     }
                  }

                  if (isAuthorized) {
                     System.out.print(String.format("\nAre you sure you want to delete orderid '%d' (Y/N): ", inputOrderID));
                     inputOrderString = in.readLine();
                     if (inputOrderString.equals("Yes") || inputOrderString.equals("yes") || inputOrderString.equals("Y") || inputOrderString.equals("y")) {
                        String deletionUpdate = String.format("DELETE FROM ItemStatus WHERE orderid = '%d'", inputOrderID);
                        esql.executeUpdate(deletionUpdate);
                        deletionUpdate = String.format("DELETE FROM Orders WHERE orderid = '%d'", inputOrderID);
                        esql.executeUpdate(deletionUpdate);
                        System.out.println(String.format("\nDeleted orderid '%d' successfully.", inputOrderID));
                     }
                     else {
                        System.out.print(String.format("\nCancelling deletion of orderid '%d'...", inputOrderID));
                     }
                  }
                  break;
               case 3:
                  if (!isManager) {
                     System.out.println("Unrecognized choice!");
                  }
                  else {
                     query = String.format("SELECT * FROM Orders WHERE timeStampRecieved > (SELECT NOW()- interval '1 day') ORDER BY timestamprecieved DESC");
                     numRows = esql.executeQueryAndPrintResult(query);
                     System.out.println(String.format("(%d items)", numRows));
                  }
                  break;
               case 9:
                  usermenu = false;
                  break;
               default:
                  System.out.println("Unrecognized choice!");
                  break;
            }
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

}// end Cafe