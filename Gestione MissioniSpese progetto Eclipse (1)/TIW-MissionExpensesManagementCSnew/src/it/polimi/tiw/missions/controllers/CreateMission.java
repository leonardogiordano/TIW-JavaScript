package it.polimi.tiw.missions.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.missions.beans.User;
import it.polimi.tiw.missions.dao.MissionsDAO;
import it.polimi.tiw.missions.utils.ConnectionHandler;

@WebServlet("/CreateMission")
@MultipartConfig

public class CreateMission extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateMission() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	private Date getMeYesterday() {
		return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Get and parse all parameters from request
		boolean isBadRequest = false;
		Date startDate = null;
		String destination = null;
		String description = null;
		Integer days = null;
		String country = null;
		String province = null;
		String city = null;
		String fund = null;

		try {
			days = Integer.parseInt(request.getParameter("days"));
			destination = StringEscapeUtils.escapeJava(request.getParameter("destination"));
			description = StringEscapeUtils.escapeJava(request.getParameter("description"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			startDate = (Date) sdf.parse(request.getParameter("date"));
			country = StringEscapeUtils.escapeJava(request.getParameter("country"));
			province = StringEscapeUtils.escapeJava(request.getParameter("province"));
			city = StringEscapeUtils.escapeJava(request.getParameter("city"));
			fund = StringEscapeUtils.escapeJava(request.getParameter("fund"));

			isBadRequest = days <= 0 || destination.isEmpty() || description.isEmpty()
					|| getMeYesterday().after(startDate) || country.isEmpty() || province.isEmpty() || city.isEmpty()
					|| fund.isEmpty();
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		if (isBadRequest) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}

		// Create mission in DB
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		MissionsDAO missionsDAO = new MissionsDAO(connection);
		int newMissionId;
		try {
			newMissionId = missionsDAO.createMission(startDate, days, destination, description, country, province, city,
					fund, user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create mission");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(newMissionId);

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
