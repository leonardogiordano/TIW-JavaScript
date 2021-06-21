package it.polimi.tiw.missions.beans;

import java.sql.Date;

public class Mission {
	private int id;
	private Date startDate;
	private String destination;
	private MissionStatus status;
	private String description;
	private int days;
	private String country;
	private String province;
	private String city;
	private String fund;
	private int reporterId;
	private ExpenseReport expenses;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MissionStatus getStatus() {
		return status;
	}

	public void setStatus(MissionStatus status) {
		this.status = status;
	}

	public void setStatus(int value) {
		this.status = MissionStatus.getMissionStatusFromInt(value);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setProvince(String prov) {
		this.province = prov;
	}

	public String getProvince() {
		return province;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setFund(String fund) {
		this.fund = fund;
	}

	public String getFund() {
		return fund;
	}

	public int getReporterId() {
		return reporterId;
	}

	public void setReporterId(int reporter) {
		this.reporterId = reporter;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public ExpenseReport getExpenses() {
		return expenses;
	}

	public void setExpenses(ExpenseReport er) {
		expenses = er;
	}

	public boolean isOpen() {
		return status == MissionStatus.OPEN;
	}

	public boolean isReported() {
		return status == MissionStatus.REPORTED;
	}

	public boolean isClosed() {
		return status == MissionStatus.CLOSED;
	}

}
