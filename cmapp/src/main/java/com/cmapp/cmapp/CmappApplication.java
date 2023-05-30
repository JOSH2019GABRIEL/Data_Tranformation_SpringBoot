package com.cmapp.cmapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@SpringBootApplication
@EnableScheduling
public class CmappApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(CmappApplication.class, args);
//		Connection h2con = connectToH2DB();
//		Connection postgresCon = connectToPostgresDB();
	}

	//Postgresql conncection
	public static Connection connectToPostgresDB() {
		final String url = "jdbc:postgresql://localhost/client_management";
		final String user = "postgres";
		final String password = "admin";

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Connected to the PostgreSQL server successfully.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conn;
	}

	//H2 conncection
	public static  Connection connectToH2DB() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:h2:file:C:/LAMIS3/database/lamis", "admin", "admin");
			System.out.println("Connected to the H2DB server successfully.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conn;
	}

	//Server MysQL
	public static Connection connectToMysqlDB() {
		final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
		final String url = "jdbc:mysql://hostname:port/database_name";
		final String user = "postgres";
		final String password = "admin";

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, user, password);
			System.out.println("Connected to the PostgreSQL server successfully.");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

		return conn;
	}

	// Unscramble Method
	public static String unscrambleCharacters(String string) {
		if(string == null) return "";
		string = string.replace("^", "a");
		string = string.replace("~", "b");
		string = string.replace("`", "c");
		string = string.replace("*", "e");
		string = string.replace("$", "f");
		string = string.replace("#", "g");
		string = string.replace("@", "h");
		string = string.replace("!", "i");
		string = string.replace("%", "j");
		string = string.replace("|", "k");
		string = string.replace("}", "n");
		string = string.replace("{", "o");
		return string;
	}

	public static String unscrambleNumbers(String string) {
		if(string == null) return "";
		string = string.replace("^", "1");
		string = string.replace("~", "2");
		string = string.replace("`", "3");
		string = string.replace("*", "5");
		string = string.replace("$", "6");
		string = string.replace("#", "7");
		string = string.replace("@", "8");
		string = string.replace("!", "9");
		return string;
	}

	//Client Data Transfer

	public static boolean insertClients(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try {
			stat = h2Connection.createStatement();
			rs = stat.executeQuery("select p.patient_id, p.surname, p.other_names, p.hospital_num, p.id_uuid as p_uuid, p.gender, CASE WHEN p.date_birth is null THEN '0001-01-01' ELSE p.date_birth END AS date_birth, p.phone, p.address, p.state, p.lga, p.ward," +
					"p.marital_status, p.education, p.pregnant, CASE WHEN p.date_started is null THEN '0001-01-01' ELSE p.date_started END AS date_started, p.regimentype, p.regimen, CASE WHEN p.date_last_refill is null THEN '0001-01-01'" +
					"ELSE p.date_last_refill END AS date_last_refill, CASE WHEN p.date_next_refill is null THEN '0001-01-01' ELSE p.date_next_refill END AS date_next_refill, p.current_status, p.last_viral_load, d.dsd_type," +
					"CASE WHEN c.ever_had_covid19_dose = 'Yes' THEN 'VACCINATED' ELSE 'NOT VACCINATED' END AS ever_had_covid19_dose, p.time_stamp, cm.uuid as cm_uuid, p.facility_id, CASE WHEN p.date_last_viral_load is null THEN '0001-01-01'" +
					"ELSE p.date_last_viral_load END AS date_last_viral_load,cxca.screening_result,p.tb_status,'0001-01-01' as date_collected, CASE WHEN p.date_last_viral_load is null THEN '0001-01-01'" +
					"ELSE p.date_last_viral_load END AS date_last_viral_load, la.comment, '0' as biometricCount, '0' as validCount, p.next_kin,p.address_kin, p.phone_kin FROM PATIENT As p left JOIN covid19 As c ON p.patient_id = c.patient_id left join devolve as d on p.patient_id = d.patient_id" +
					" left join casemanager as cm on p.casemanager_id=cm.casemanager_id left join cervicalscreening as cxca ON p.patient_id = cxca.patient_id left join laboratory as la on la.patient_id = p.patient_id where cm.uuid is not null and p.date_last_refill is not null and  p.date_last_refill > DATEADD('MONTH',-18, CURRENT_DATE);");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("insert IGNORE into client (id, surname, other_names, hospital_num, uuid, sex, dob, phone, address, state_of_residence, lga_of_residence, ward_of_residence," +
							"marital_status, educational_status, pregnancy_status, art_start_date, current_regimen, current_regimen_line, last_refill_date, next_refill_date, current_status, last_viral_load, dsd_model, covid_vac_status," +
							"updated_at, casemanager_uuid, facility_id, date_last_viral_load,  cervical_screening_status, tb_screening_status,date_last_vl_sample_collection,date_last_vl_sample_collection_with_result,viral_load_indication," +
							"biometric_fingers_captured, biometric_valid_captures, next_of_kin_name, next_of_kin_address, next_of_kin_phone) VALUES ('"+ rs.getString("patient_id")+"','"+unscrambleCharacters(rs.getString("surname"))+"','"+unscrambleCharacters(rs.getString("other_names"))+"','"+rs.getString("hospital_num")+"','"+
							rs.getString("p_uuid")+"','"+rs.getString("gender")+"','"+rs.getString("date_birth")+"','"+unscrambleNumbers(rs.getString("phone"))+"','"+unscrambleCharacters(rs.getString("address"))+"','"+rs.getString("state")+"','"+
							rs.getString("lga")+"','"+rs.getString("ward")+"','"+rs.getString("marital_status")+"','"+ rs.getString("education")+"','"+rs.getString("pregnant")+"','"+rs.getString("date_started")+"','"+
							rs.getString("regimen")+"','"+rs.getString("regimentype")+"','"+rs.getString("date_last_refill")+"','"+rs.getString("date_next_refill")+"','"+rs.getString("current_status")+"','"+
							rs.getString("last_viral_load")+"','"+rs.getString("dsd_type")+"','"+rs.getString("ever_had_covid19_dose")+"','"+rs.getString("time_stamp")+"','"+rs.getString("cm_uuid")+"','"+
							rs.getString("facility_id")+"','"+rs.getString("date_last_viral_load")+"','"+rs.getString("screening_result")+"','"+rs.getString("tb_status")+"','"+rs.getString("date_collected")+"','"+rs.getString("date_last_viral_load")+"','"+
							rs.getString("comment")+"','"+rs.getString("biometricCount")+"','"+rs.getString("validCount")+"','"+unscrambleCharacters(rs.getString("next_kin"))+"','"+unscrambleCharacters(rs.getString("address_kin"))+"','"+unscrambleNumbers(rs.getString("phone_kin"))+"');");



				} catch (Exception e) {
					System.out.println(rs.getString("patient_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e) {
			System.out.println("Errors encountered Patient Records insertion shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (stat != null) stat.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("THis is done here*****.");
		return status;
	}

	//Update Some records after insert Clients into the New Database
	public static boolean insertUpdate(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try {
			stat = h2Connection.createStatement();
			rs = stat.executeQuery("SELECT b.hospital_num, COUNT(b.hospital_num) AS biometricCountz, SUM(CASE b.is_valid WHEN 1 THEN 1 ELSE NULL END) as validCountz FROM biometric as b  group by b.hospital_num");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("update client set biometric_fingers_captured= "+rs.getString("biometricCountz")+", biometric_valid_captures ="+rs.getString("validCountz")+" where hospital_num='"+rs.getString("hospital_num")+"'");


				} catch (Exception e) {
					System.out.println(rs.getString("hospital_num"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e) {
			System.out.println("Errors encountered Patient Records insertion shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (stat != null) stat.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return status;
	}
	//To Update Latest Sample Collection of Each Client in the DB
	public static boolean insertSampleUpdate(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try {
			stat = h2Connection.createStatement();
			rs = stat.executeQuery(" SELECT patient_id, MAX(date_collected) AS date_collected FROM laboratory WHERE labtest_id = 16 AND date_collected IS NOT NULL GROUP BY patient_id");
			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("update client set date_last_vl_sample_collection= '"+rs.getString("date_collected")+"' where id ='"+rs.getString("patient_id")+"'");
				} catch (Exception e) {
					System.out.println(rs.getString("p.patient_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e) {
			System.out.println("Errors encountered Patient Records insertion shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (stat != null) stat.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return status;
	}
	//Insert Case Manager to
	public static boolean insertPerson(Connection h2Connection, Connection postgresConnection) throws SQLException {
		boolean status = false;
		Statement h2Stat = null;
		ResultSet rs = null;

		try {
			h2Stat = h2Connection.createStatement();
			rs = h2Stat.executeQuery("select casemanager_id,CASE WHEN uuid is null THEN random_uuid() ELSE uuid END AS uuid, Substr(fullname,1, Instr(fullname,' ')) as surname, Substr(fullname, Instr(fullname, ' ')) as first_name , '' as other_names, sex, '1901-12-12' as age,address,'casemanager@Email.com' as Email,"+
					"phone_number, CASE WHEN time_stamp is null THEN CURRENT_TIMESTAMP() ELSE time_stamp END AS updated_at, CASE WHEN time_stamp is null THEN CURRENT_TIMESTAMP() ELSE time_stamp END AS created_at, time_stamp As updated_at, 'NO' as archived, 'admin' as created_by, 'admin' as updated_by, facility_id from casemanager");

			PreparedStatement ps = postgresConnection.prepareStatement("insert into person (id, uuid, surname, first_name,other_names,sex, dob,address, email, telephone, created_at, updated_at, archived, created_by, updated_by, facility_id)" +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			while (rs.next()) {
				try {
					// Check if record already exists in PostgresSQL
					PreparedStatement checkPs = postgresConnection.prepareStatement("SELECT * FROM person WHERE id = ?");
					checkPs.setInt(1, rs.getInt("casemanager_id"));
					ResultSet checkRs = checkPs.executeQuery();

					if (!checkRs.next()) {
						// Record doesn't exist, insert it
						ps.setInt(1, rs.getInt("casemanager_id"));
						ps.setObject(2, UUID.fromString(rs.getString("uuid")));
						ps.setString(3, rs.getString("surname"));
						ps.setString(4, rs.getString("first_name"));
						ps.setString(5, rs.getString("other_names"));
						ps.setString(6, rs.getString("sex"));
//                        ps.setString(7, rs.getString("age"));
						ps.setDate(7, java.sql.Date.valueOf(rs.getString("age")));
						ps.setString(8, rs.getString("address"));
						ps.setString(9, rs.getString("Email"));
						ps.setString(10, rs.getString("phone_number"));
//                        ps.setString(11, rs.getString("created_at"));
//                        ps.setString(12, rs.getString("updated_at"));
						ps.setTimestamp(11, Timestamp.valueOf(rs.getString("created_at")));
						ps.setTimestamp(12, Timestamp.valueOf(rs.getString("updated_at")));

//                        ps.setString(13, rs.getString("archived"));
						ps.setBoolean(13, Boolean.parseBoolean(rs.getString("archived")));

						ps.setString(14, rs.getString("created_by"));
						ps.setString(15, rs.getString("updated_by"));
						ps.setInt(16, rs.getInt("facility_id"));

						ps.executeUpdate();
						System.out.println("Processing Casemanager with ID " +rs.getString("casemanager_id"));
					}
				} catch (Exception e) {
					System.out.println(rs.getString("casemanager_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e) {
			System.out.println("Errors encountered Person Records insertion shutting down.");
			System.out.println(e);
		} finally {
			try {
				System.out.println("Data Transfer completely Done...");
				if (rs != null) rs.close();
				if (h2Stat != null) h2Stat.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return status;
	}
	//Appointments Methods
	public static boolean insertArvAppointments(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		PreparedStatement ps = null;
		ResultSet rs = null;
		System.out.println("Moving ARV Appointments****************************************");

		try {
			ps = postgresConnection.prepareStatement("SELECT id FROM appointment WHERE id=?");
			PreparedStatement psInsert = postgresConnection.prepareStatement("INSERT INTO appointment " +
					"(id, uuid, appointment_date, date_appointemnt_held, longitude, latitude, facility_id, created_by, updated_by, created_at, update_at, appointment_type_id, client_id) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			Statement stat = h2Connection.createStatement();
			rs = stat.executeQuery("SELECT pharmacy_id, " +
					"CASE WHEN id_uuid IS NULL THEN random_uuid() ELSE id_uuid END AS uuid, " +
					"next_appointment, date_visit, " +
					"'' AS longitude, " +
					"'' AS latitude, " +
					"facility_id, " +
					"'admin' AS created_by, " +
					"'admin' AS updated_by, " +
					"time_stamp AS created_at, " +
					"time_stamp AS updated_at, " +
					"1 AS appointment_type_id, " +
					"patient_id " +
					"FROM pharmacy " +
					"WHERE date_visit > DATEADD('MONTH', -6, CURRENT_DATE)");

			while (rs.next()) {
				Integer id = rs.getInt("pharmacy_id");
				// Check if the appointment already exists in the PostgreSQL database
				ps.setInt(1, id);
				ResultSet result = ps.executeQuery();
				if (result.next()) {
					continue; // skip if the record already exists
				}

				try {
					// Insert the record if it doesn't exist
					psInsert.setInt(1, rs.getInt("pharmacy_id"));
//                    psInsert.setString(1, id);
					psInsert.setObject(2, UUID.fromString(rs.getString("uuid")));
//                    psInsert.setString(2, rs.getString("uuid"));
					psInsert.setDate(3, rs.getDate("next_appointment"));
					psInsert.setDate(4, rs.getDate("date_visit"));
					psInsert.setString(5, rs.getString("longitude"));
					psInsert.setString(6, rs.getString("latitude"));
					psInsert.setInt(7, rs.getInt("facility_id"));
					psInsert.setString(8, rs.getString("created_by"));
					psInsert.setString(9, rs.getString("updated_by"));
					psInsert.setTimestamp(10, rs.getTimestamp("created_at"));
					psInsert.setTimestamp(11, rs.getTimestamp("updated_at"));
					psInsert.setInt(12, rs.getInt("appointment_type_id"));
					psInsert.setInt(13, rs.getInt("patient_id"));
					psInsert.executeUpdate();
					System.out.println("Processing appointment with ID " + id);
				} catch (SQLException e) {
					System.out.println(id);
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}
				status = true;
			}
		} catch (SQLException e) {
			System.out.println("Errors encountered ARV appointments insertion shutting down.");
			System.out.println(e.getMessage());
		} finally {
			try {
				System.out.println("Data Transfer completely Done...");
				if (rs != null) rs.close();
				if (ps != null) ps.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return status;
	}

	public static boolean insertEACAppointments(Connection h2Connection, Connection postgresConnection){
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try{
			stat = h2Connection.createStatement();
			rs = stat.executeQuery("Select  eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC1 AS appointment_date, DATE_EAC1 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC1 IS NOT NULL  union all\n" +
					"Select eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC2 AS appointment_date, DATE_EAC2 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC2 IS NOT NULL union all\n" +
					"select eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC3 AS appointment_date, DATE_EAC3 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC3 IS NOT NULL union all\n" +
					"Select eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC4 AS appointment_date, DATE_EAC4 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC4 IS NOT NULL union all\n" +
					"select eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC5 AS appointment_date, DATE_EAC5 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC5 IS NOT NULL union all\n" +
					"Select eac_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, DATE_EAC6 AS appointment_date, DATE_EAC6 AS date_appointment_held,null as longitude, null as latitude, facility_id, 'Admin' AS created_by, 'Admin' AS updated_by, TIME_STAMP AS created_at, TIME_STAMP AS updated_at,  2 AS appointment_type_id, patient_id\n" +
					"from eac WHERE DATE_EAC6 IS NOT NULL");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("insert into APPOINTMENT (id, uuid, appointment_date, date_appointemnt_held, longitude, latitude, facility_id, created_by, updated_by, created_at, update_at, appointment_type_id, client_id)" +
							"VALUES ("+rs.getString("eac_id")+", '"+rs.getString("uuid")+"', '"+rs.getString("appointment_date")+"','"+
							rs.getString("date_appointment_held")+"', '"+rs.getString("longitude")+"', '" +rs.getString("latitude")+"', '"+
							rs.getString("facility_id")+"', '"+ rs.getString("created_by")+"', '"+rs.getString("updated_by")+"', '"+
							rs.getString("created_at")+"', '"+rs.getString("updated_at")+"',"+ rs.getString("appointment_type_id")+","+rs.getString("patient_id")+");");
				} catch (Exception e){
					System.out.println(rs.getString("eac_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}
				status = true;
			}
		} catch (Exception e){
			System.out.println("Errors encountered EAC appointments insertion shutting down.");
			System.out.println(e.getMessage());
		}
		return status;
	}

	public static boolean insertVLAppointments(Connection h2Connection, Connection postgresConnection){
		boolean status = false;

		try{
			Statement stat = h2Connection.createStatement();
			ResultSet rs = stat.executeQuery("select laboratory_id, CASE WHEN id_uuid is null THEN random_uuid() ELSE id_uuid END AS uuid, date_reported, date_collected, 'longitude' as longitude, 'latitude' as latitude , facility_id," +
					" 'admin' as created_by, 'admin' as updated_by , time_stamp AS created_at, time_stamp AS updated_at, 3 AS appointment_type_id, patient_id from laboratory where date_reported is  not null and " +
					"  date_reported > DATEADD('YEAR',-1, CURRENT_DATE);");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("insert into APPOINTMENT (id, uuid, appointment_date, date_appointemnt_held, longitude, latitude, facility_id, created_by, updated_by, created_at, update_at, appointment_type_id, client_id)" +
							"VALUES ("+rs.getString("laboratory_id")+", '"+rs.getString("uuid")+"', '"+rs.getString("date_reported")+"','"+
							rs.getString("date_collected")+"', '"+rs.getString("longitude")+"', '" +rs.getString("latitude")+"', '"+
							rs.getString("facility_id")+"', '"+ rs.getString("created_by")+"', '"+rs.getString("updated_by")+"', '"+
							rs.getString("created_at")+"', '"+rs.getString("updated_at")+"',"+ rs.getString("appointment_type_id")+","+rs.getString("patient_id")+");");
				} catch (Exception e){
					System.out.println(rs.getString("laboratory_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}
				status = true;
			}
		} catch (Exception e){
			System.out.println("Errors encountered ARV appointments insertion shutting down.");
			System.out.println(e.getMessage());
		}
		return status;
	}
	// This Methods Auto Complete some of the table in the PostGresql and the Server DB
	public static boolean insertOthers(Connection postgresConnection){
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try{
			stat = postgresConnection.createStatement();
			//this part update person data extracted from h2 to postgreSQL person table then updating usr_user_id on person table extraction from usr_user table
			stat.executeUpdate("update person set usr_user_id = "+ "(select id from usr_user where person.uuid=usr_user.uuid limit 1)");
			//this part insert data from h2 to postgreSQL table rel_facility__usr_user
			stat.executeUpdate("insert into rel_facility__usr_user(usr_user_id, facility_id) select id, default_facility_id from usr_user as us where NOT EXISTS (SELECT usr_user_id, facility_id FROM rel_facility__usr_user as rf where rf.facility_id = us.default_facility_id);");
			//this part insert data from USR_SUSER to rel_project__facility on postgreSQL table
			stat.executeUpdate("insert into rel_project__facility (facility_id, project_id) select DISTINCT default_facility_id, 1 from usr_user as us where NOT EXISTS (SELECT facility_id, project_id FROM rel_project__facility as rf where rf.facility_id = us.default_facility_id);");
			stat.executeUpdate("insert into rel_project__usr_user(usr_user_id, project_id) select id, 1 from usr_user as us where NOT EXISTS (SELECT usr_user_id, project_id FROM rel_project__usr_user as rf where rf.usr_user_id = us.id);");
			stat.executeUpdate("insert into rel_usr_user__permission(permission_id, usr_user_id) select 1301, id from usr_user as us where NOT EXISTS (SELECT permission_id, usr_user_id FROM rel_usr_user__permission as rp where rp.usr_user_id = us.id AND permission_id = 1301);");
			stat.executeUpdate("insert into rel_usr_user__permission(permission_id, usr_user_id) select 1303, id from usr_user as us where NOT EXISTS (SELECT permission_id, usr_user_id FROM rel_usr_user__permission as rp where rp.usr_user_id = us.id AND permission_id = 1303);");
			stat.executeUpdate("insert into rel_usr_user__permission(permission_id, usr_user_id) select 1451, id from usr_user as us where NOT EXISTS (SELECT permission_id, usr_user_id FROM rel_usr_user__permission as rp where rp.usr_user_id = us.id AND permission_id = 1451);");
		} catch (Exception e){
			System.out.println("Errors encountered Other PostgresQL insertion shutting down.");
			System.out.println(e.getMessage());
		}
		return status;
	}

	//This Method insert Users Into the DB
	public static boolean insertUsers(Connection postgresConnection){
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try{
			stat = postgresConnection.createStatement();

			stat.executeUpdate("insert into usr_user( uuid, username, password, created_at,updated_at, active, archived, created_by, updated_by, default_facility_id, usr_role_id, implementing_partner_id)" +
					"select uuid, Concat(left(split_part(surname, ' ', 1), 1)," +
					"left(split_part(surname, ' ', 2),1 ), FLOOR(RANDOM()*1000000)), 12345678, created_at, NOW(), true, false, created_by, updated_by, facility_id, 1003,2 from  person as p where NOT EXISTS" +
					"(SELECT uuid FROM usr_user as us where us.uuid = p.uuid);");
		} catch (Exception e){
			System.out.println("Errors encountered Other PostgresQL insertion shutting down.");
			System.out.println(e.getMessage());
		}
		return status;
	}

	// This Method Transfer Children <19 to Children Table
	public static boolean insertChildren(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try {
			stat = h2Connection.createStatement();
			rs = stat.executeQuery("select patient_id, CASE WHEN uuid is null THEN random_uuid() ELSE uuid END AS uuid, patient_id as client_id, id_uuid, surname, other_names,date_birth, age, gender, null as testing_status," +
					"'admin' as created_by, 'admin' as updated_by,CASE WHEN time_stamp is null THEN CURRENT_TIMESTAMP() ELSE time_stamp END AS created_at,CASE WHEN time_stamp is null THEN CURRENT_TIMESTAMP() ELSE time_stamp END AS updated_at from patient " +
					"WHERE age <= 17 and date_last_refill > DATEADD('MONTH',-18, CURRENT_DATE);");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {
					stat2.executeUpdate("insert into children (id, uuid, client_id, client_uuid, last_name, other_name, dob, age, sex, testing_status, created_by, update_by, created_at, updated_at) " +
							"VALUES ('"+ rs.getString("patient_id")+"','"+ rs.getString("uuid")+"','"+ rs.getString("client_id")+"','"+ rs.getString("id_uuid")+"','"+unscrambleCharacters(rs.getString("surname"))+"','"+unscrambleCharacters(rs.getString("other_names"))+"','"+ rs.getString("date_birth")+"','"+
							rs.getString("age")+"','"+ rs.getString("gender")+"','"+rs.getString("testing_status")+"','"+rs.getString("created_by")+"','"+rs.getString("updated_by")+"','"+(rs.getString("created_at"))+"','"+(rs.getString("updated_at"))+"');");

//                    stat2.executeUpdate("update children set client_id = "+ "(select id from usr_user where person.uuid=usr_user.uuid limit 1");


				} catch (Exception e) {
					System.out.println(rs.getString("patient_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e) {
			System.out.println("Errors encountered Patient Records insertion shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (stat != null) stat.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		System.out.println("THis is done here*****.");
		return status;

	}
	// This Method move Covid data to the other DB
	public static boolean insertCovid19(Connection h2Connection, Connection postgresConnection){
		boolean status = false;
		Statement stat = null;
		ResultSet rs = null;

		try{
			stat = h2Connection.createStatement();
			rs = stat.executeQuery("select co.covid19_id, co.uuid, co.patient_id, p.id_uuid,co.vaccine_Type1,co.date_of_covid19_vaccine1 , 'admin' as created_by, 'admin' as updated_by,CASE WHEN co.time_stamp is null THEN CURRENT_TIMESTAMP() ELSE co.time_stamp END AS created_at , now() as updated_at " +
					"from covid19 as co inner join patient as p on p.patient_id = co.patient_id");

			Statement stat2 = postgresConnection.createStatement();
			while (rs.next()) {
				try {

					stat2.executeUpdate("insert into covid (id, uuid, client_id,client_uuid, vaccine_type, date_received, created_by, update_by,created_at, updated_at)" +
							"VALUES ("+rs.getString("covid19_id")+", '"+rs.getString("uuid")+"', '"+rs.getString("patient_id")+"','"+
							rs.getString("id_uuid")+"', '"+rs.getString("vaccine_type1")+"', '"+rs.getString("date_of_covid19_vaccine1")+"', '"+
							rs.getString("created_by")+"', '"+rs.getString("updated_by")+"', '"+rs.getString("created_at")+"', '"+rs.getString("updated_at")+"');");

				} catch (Exception e){
					System.out.println(rs.getString("covid19_id"));
					System.out.println("Insert failed.");
					System.out.println(e.getMessage());
				}

				status = true;
			}
		} catch (Exception e){
			System.out.println("Errors encountered Covid19 Records insertion shutting down.");
			System.out.println(e);
		}finally{
			try {
				if (rs != null) rs.close();
				if (stat != null) stat.close();
			}catch(Exception ex){ex.printStackTrace();}
		}
		System.out.println("Done Here");
		return status;
	}

	//Insert States into the New DBs
	public static boolean insertState(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement h2Statement = null;
		Statement postgresStatement = null;
		ResultSet rs = null;

		System.out.println("Moving States *************************************************");

		try {
			h2Statement = h2Connection.createStatement();
			postgresStatement = postgresConnection.createStatement();

			// Retrieve state records from H2 database
			rs = h2Statement.executeQuery("SELECT state_id, name FROM state");

			while (rs.next()) {
				String stateId = rs.getString("state_id");
				String name = rs.getString("name");

				try {
					// Check if state record already exists in Postgres database
					ResultSet existingRecord = postgresStatement.executeQuery("SELECT id FROM state WHERE id = '" + stateId + "'");
					boolean recordExists = existingRecord.next();

					if (!recordExists) {
						// Insert state record into Postgres database
						postgresStatement.executeUpdate("INSERT INTO state (id, name) VALUES ('" + stateId + "', '" + name + "')");

						System.out.println("Insert Successful for state with id " + stateId);
					}

					status = true;
				} catch (Exception e) {
					System.out.println("Insert failed for state with id " + stateId);
					System.out.println(e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println("Errors encountered during state records insertion. Shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (h2Statement != null) h2Statement.close();
				if (postgresStatement != null) postgresStatement.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return status;
	}


	//This Method Insert Facilities into the new DB
	public static boolean insertFacility(Connection h2Connection, Connection postgresConnection) {
		boolean status = false;
		Statement h2Statement = null;
		Statement postgresStatement = null;
		ResultSet rs = null;

		System.out.println("********************* Moving Facilities *******************");

		try {
			h2Statement = h2Connection.createStatement();
			postgresStatement = postgresConnection.createStatement();

			rs = h2Statement.executeQuery("SELECT f.facility_id, f.name, f.address1, f.datim_id, f.longitude, f.latitude, CURRENT_TIMESTAMP() AS created_at, CURRENT_TIMESTAMP() AS updated_at, 'NO' AS archived, 'admin' AS created_by, 'admin' AS updated_by, f.state_id, f.lga_id, w.ward_id FROM facility AS f LEFT JOIN ward AS w ON f.lga_id = w.lga_id");

			while (rs.next()) {
				String facilityId = rs.getString("facility_id");

				try {
					// Check if facility record already exists in Postgres database
					ResultSet existingRecord = postgresStatement.executeQuery("SELECT id FROM facility WHERE id = '" + facilityId + "'");
					boolean recordExists = existingRecord.next();

					if (!recordExists) {
						// Insert facility record into Postgres database
						postgresStatement.executeUpdate("INSERT INTO facility (id, name, address, datim_code, longitude, latitude, created_at, updated_at, archived, created_by, updated_by, state_id, lga_id, ward_id) VALUES ('" + facilityId + "', '" + rs.getString("name") + "', '" + rs.getString("address1") + "', '" + rs.getString("datim_id") + "', '" + rs.getString("longitude") + "', '" + rs.getString("latitude") + "', '" + rs.getString("created_at") + "', '" + rs.getString("updated_at") + "', '" + rs.getString("archived") + "', '" + rs.getString("created_by") + "', '" + rs.getString("updated_by") + "', '" + rs.getString("state_id") + "', '" + rs.getString("lga_id") + "', '" + rs.getString("ward_id") + "')");
						System.out.println("Insert Success for facility with id " + facilityId);
					}

					status = true;
				} catch (Exception e) {
					System.out.println("Insert failed for facility with id " + facilityId);
					System.out.println(e.getMessage());
				}
			}
		} catch (Exception e) {
			System.out.println("Errors encountered during facility records insertion. Shutting down.");
			System.out.println(e);
		} finally {
			try {
				if (rs != null) rs.close();
				if (h2Statement != null) h2Statement.close();
				if (postgresStatement != null) postgresStatement.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return status;
	}

	@Override
	public void run(String... args) throws Exception {
		Connection h2con = connectToH2DB();
		Connection postgresCon = connectToPostgresDB();
		insertPerson(h2con, postgresCon);
	}
//	@Bean
//	public CommandLineRunner run(Connection h2Connection, Connection postgresConnection) throws Exception {
//		return args -> {
//			Statement stat = h2Connection.createStatement();
//			// Fetch data from H2 database
//			ResultSet rs =
//			List<Map<String, Object>> data = stat.executeUpdate("SELECT * FROM table_name");
//
//			// Save data to local Postgresql database
//			for (Map<String, Object> row : data) {
//				jdbcTemplate.update("INSERT INTO local_table_name (column1, column2) VALUES (?, ?)",
//						row.get("column1"), row.get("column2"));
//			}
//
//			// Move data to Postgresql cloud database
//			for (Map<String, Object> row : data) {
//				jdbcTemplate.update("INSERT INTO cloud_table_name (column1, column2) VALUES (?, ?)",
//						row.get("column1"), row.get("column2"));
//			}
//		};
//	}

}
