package it.polimi.tiw.missions.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.missions.beans.Mission;
import it.polimi.tiw.missions.beans.MissionStatus;
import it.polimi.tiw.missions.beans.User;
import it.polimi.tiw.missions.dao.MissionsDAO;
import it.polimi.tiw.missions.utils.ConnectionHandler;

@WebServlet("/CloseMission")
@MultipartConfig
public class CloseMission extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CloseMission() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// get and check params
		Integer missionId = null;
		try {
			missionId = Integer.parseInt(request.getParameter("missionid"));
		} catch (NumberFormatException | NullPointerException e) {
			// for debugging only e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}

		// Update mission status
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		MissionsDAO missionsDao = new MissionsDAO(connection);

		try {
			// Check that only the user who created the mission edits it
			Mission mission = missionsDao.findMissionById(missionId);
			if (mission == null) {
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				response.getWriter().println("Mission not found");
				return;
			}
			if (mission.getReporterId() != user.getId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Not allowed");
				return;
			}
			missionsDao.changeMissionStatus(missionId, MissionStatus.CLOSED);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Mission not closable");		
			return;
		}

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
