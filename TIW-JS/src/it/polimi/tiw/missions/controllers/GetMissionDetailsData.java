package it.polimi.tiw.missions.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.lang.String;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import it.polimi.tiw.missions.beans.Mission;
import it.polimi.tiw.missions.beans.User;
import it.polimi.tiw.missions.dao.ExpenseReportDAO;
import it.polimi.tiw.missions.dao.MissionsDAO;
import it.polimi.tiw.missions.utils.ConnectionHandler;

@WebServlet("/GetMissionDetailsData")
public class GetMissionDetailsData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetMissionDetailsData() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// get and check params
		Integer missionId = null;
		try {
			missionId = Integer.parseInt(request.getParameter("missionid"));
		} catch (NumberFormatException | NullPointerException e) {
			// only for debugging e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}

		// If a mission with that ID exists for that USER,
		// obtain the expense report for it
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		MissionsDAO missionsDAO = new MissionsDAO(connection);
		Mission mission = null;
		try {
			mission = missionsDAO.findMissionById(missionId);
			if (mission == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Resource not found");
				return;
			}
			if (mission.getReporterId() != user.getId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("User not allowed");
				return;
			}
			ExpenseReportDAO expenseReportDAO = new ExpenseReportDAO(connection);
			mission.setExpenses(expenseReportDAO.findExpensesForMission(missionId));
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover mission");
			return;
		}

		// Redirect to the Home page and add missions to the parameters
		String json = new Gson().toJson(mission);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
