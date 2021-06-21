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


import it.polimi.tiw.missions.beans.ExpenseReport;
import it.polimi.tiw.missions.beans.Mission;
import it.polimi.tiw.missions.beans.User;
import it.polimi.tiw.missions.dao.ExpenseReportDAO;
import it.polimi.tiw.missions.dao.MissionsDAO;
import it.polimi.tiw.missions.exceptions.BadMissionForExpReport;
import it.polimi.tiw.missions.utils.ConnectionHandler;

@WebServlet("/CreateExpensesReport")
@MultipartConfig
public class CreateExpensesReport extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateExpensesReport() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Check params are present and correct
		ExpenseReport expenseReport = null;
		Integer missionId = null;
		try {
			missionId = Integer.parseInt(request.getParameter("missionid"));
			double food = Double.parseDouble(request.getParameter("food"));
			double accomodation = Double.parseDouble(request.getParameter("accomodation"));
			double transportation = Double.parseDouble(request.getParameter("transportation"));
			if (food >= 0 && accomodation >= 0 && transportation >= 0) {
				expenseReport = new ExpenseReport(missionId, food, accomodation, transportation);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Incorrect param values");
				return;
			}
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		// Execute controller logic
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		ExpenseReportDAO expenseReportDAO = new ExpenseReportDAO(connection);
		MissionsDAO missionsDao = new MissionsDAO(connection);
		try {
			Mission mission = missionsDao.findMissionById(missionId);
			if (mission == null) {
				response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
				response.getWriter().println("Mission not found");
				return;
			}
			if (mission != null && mission.getReporterId() != user.getId()) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("User not allowed to perform operation");
				return;
			}
			expenseReportDAO.addExpenseReport(expenseReport, mission);
		} catch (SQLException e1) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create report");
			return;
		} catch (BadMissionForExpReport e2) {
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
			response.getWriter().println("Not allowed");
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
