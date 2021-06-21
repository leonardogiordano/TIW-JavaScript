package it.polimi.tiw.missions.dao;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.missions.beans.Mission;
import it.polimi.tiw.missions.beans.MissionStatus;

public class MissionsDAO {
	private Connection connection;

	public MissionsDAO(Connection connection) {
		this.connection = connection;
	}

	public List<Mission> findMissionsByUser(int userId) throws SQLException {
		List<Mission> missions = new ArrayList<Mission>();

		String query = "SELECT * FROM db_gestione_spese.mission where reporter = ? ORDER BY date DESC";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, userId);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Mission mission = new Mission();
					mission.setId(result.getInt("id"));
					mission.setStartDate(result.getDate("date"));
					mission.setDestination(result.getString("destination"));
					mission.setStatus(result.getInt("status"));
					mission.setDescription(result.getString("description"));
					mission.setDays(result.getInt("days"));
					mission.setCountry(result.getString("country"));
					mission.setProvince(result.getString("province"));
					mission.setCity(result.getString("city"));
					mission.setFund(result.getString("fund"));
					mission.setReporterId(userId);
					missions.add(mission);
				}
			}
		}
		return missions;
	}

	public Mission findMissionById(int missionId) throws SQLException {
		Mission mission = null;

		String query = "SELECT * FROM mission WHERE id = ?";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, missionId);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					mission = new Mission();
					mission.setId(result.getInt("id"));
					mission.setStartDate(result.getDate("date"));
					mission.setDestination(result.getString("destination"));
					mission.setStatus(result.getInt("status"));
					mission.setDescription(result.getString("description"));
					mission.setDays(result.getInt("days"));
					mission.setCountry(result.getString("country"));
					mission.setProvince(result.getString("province"));
					mission.setCity(result.getString("city"));
					mission.setFund(result.getString("fund"));
					mission.setReporterId(result.getInt("reporter"));

				}
			}
		}
		return mission;
	}

	public void changeMissionStatus(int missionId, MissionStatus missionStatus) throws SQLException {
		String query = "UPDATE mission SET status = ? WHERE id = ? ";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setInt(1, missionStatus.getValue());
			pstatement.setInt(2, missionId);
			pstatement.executeUpdate();
		}
	}

	public int createMission(Date startDate, int days, String destination, String description, String country,
			String province, String city, String fund, int reporterId) throws SQLException {

		String query = "INSERT into mission (date, destination, status, description, days, country, province, city, fund, reporter) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
			pstatement.setDate(1, new java.sql.Date(startDate.getTime()));
			pstatement.setString(2, destination);
			pstatement.setInt(3, MissionStatus.OPEN.getValue());
			pstatement.setString(4, description);
			pstatement.setInt(5, days);
			pstatement.setString(6, country);
			pstatement.setString(7, province);
			pstatement.setString(8, city);
			pstatement.setString(9, fund);
			pstatement.setInt(10, reporterId);
			pstatement.executeUpdate();
// https://stackoverflow.com/questions/19873190/statement-getgeneratedkeys-method
			ResultSet generatedKeys = pstatement.getGeneratedKeys();
			if (generatedKeys.next()) {
				return generatedKeys.getInt(1);
			} else {
				throw new SQLException("Creating user failed, no ID obtained.");
			}
		}
	}

}
