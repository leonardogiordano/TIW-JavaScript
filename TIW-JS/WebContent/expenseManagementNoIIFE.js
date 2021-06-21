{ // avoid variables ending up in the global scope

	  // page components
	  let missionDetails, missionsList, wizard,
	    pageOrchestrator = new PageOrchestrator(); // main controller

	  window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "index.html";
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    } // display initial content
	  }, false);


	  // Constructors of view components

	  function PersonalMessage(_username, messagecontainer) {
	    this.username = _username;
	    this.show = function() {
	      messagecontainer.textContent = this.username;
	    }
	  }

	  function MissionsList(_alert, _listcontainer, _listcontainerbody) {
	    this.alert = _alert;
	    this.listcontainer = _listcontainer;
	    this.listcontainerbody = _listcontainerbody;

	    this.reset = function() {
	      this.listcontainer.style.visibility = "hidden";
	    }

	    this.show = function(next) {
	      var self = this;
	      makeCall("GET", "GetMissionsData", null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var missionsToShow = JSON.parse(req.responseText);
	              if (missionsToShow.length == 0) {
	                self.alert.textContent = "No missions yet!";
	                return;
	              }
	              self.update(missionsToShow); // self visible by closure
	              if (next) next(); // show the default element of the list if present
	            
	          } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  }
                  else {
	            self.alert.textContent = message;
	          }}
	        }
	      );
	    };


	    this.update = function(arrayMissions) {
	      var elem, i, row, destcell, datecell, linkcell, anchor;
	      this.listcontainerbody.innerHTML = ""; // empty the table body
	      // build updated list
	      var self = this;
	      arrayMissions.forEach(function(mission) { // self visible here, not this
	        row = document.createElement("tr");
	        destcell = document.createElement("td");
	        destcell.textContent = mission.destination;
	        row.appendChild(destcell);
	        datecell = document.createElement("td");
	        datecell.textContent = mission.startDate;
	        row.appendChild(datecell);
	        linkcell = document.createElement("td");
	        anchor = document.createElement("a");
	        linkcell.appendChild(anchor);
	        linkText = document.createTextNode("Show");
	        anchor.appendChild(linkText);
	        //anchor.missionid = mission.id; // make list item clickable
	        anchor.setAttribute('missionid', mission.id); // set a custom HTML attribute
	        anchor.addEventListener("click", (e) => {
	          // dependency via module parameter
	          missionDetails.show(e.target.getAttribute("missionid")); // the list must know the details container
	        }, false);
	        anchor.href = "#";
	        row.appendChild(linkcell);
	        self.listcontainerbody.appendChild(row);
	      });
	      this.listcontainer.style.visibility = "visible";

	    }

	    this.autoclick = function(missionId) {
	      var e = new Event("click");
	      var selector = "a[missionid='" + missionId + "']";
	      var anchorToClick =  // the first mission or the mission with id = missionId
	        (missionId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
	      if (anchorToClick) anchorToClick.dispatchEvent(e);
	    }

	  }

	  function MissionDetails(options) {
	    this.alert = options['alert'];
	    this.detailcontainer = options['detailcontainer'];
	    this.expensecontainer = options['expensecontainer'];
	    this.expenseform = options['expenseform'];
	    this.closeform = options['closeform'];
	    this.date = options['date'];
	    this.destination = options['destination'];
	    this.status = options['status'];
	    this.description = options['description'];
	    this.country = options['country'];
	    this.province = options['province'];
	    this.city = options['city'];
	    this.fund = options['fund'];
	    this.food = options['food'];
	    this.accomodation = options['accomodation'];
	    this.travel = options['transportation'];

	    this.registerEvents = function(orchestrator) {
	      this.expenseform.querySelector("input[type='button']").addEventListener('click', (e) => {
	        var form = e.target.closest("form");
	        if (form.checkValidity()) {
	          var self = this,
	            missionToReport = form.querySelector("input[type = 'hidden']").value;
	          makeCall("POST", 'CreateExpensesReport', form,
	            function(req) {
	              if (req.readyState == 4) {
	                var message = req.responseText;
	                if (req.status == 200) {
	                  orchestrator.refresh(missionToReport);
	                } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  }
                  else {
	                  self.alert.textContent = message;
	                }
	              }
	            }
	          );
	        } else {
	          form.reportValidity();
	        }
	      });

	      this.closeform.querySelector("input[type='button']").addEventListener('click', (event) => {
	        var self = this,
	          form = event.target.closest("form"),
	          missionToClose = form.querySelector("input[type = 'hidden']").value;
	        makeCall("POST", 'CloseMission', form,
	          function(req) {
	            if (req.readyState == 4) {
	              var message = req.responseText;
	              if (req.status == 200) {
	                orchestrator.refresh(missionToClose);
	              } else if (req.status == 403) {
                  	window.location.href = req.getResponseHeader("Location");
                  	window.sessionStorage.removeItem('username');
                  }
                  else {
	                self.alert.textContent = message;
	              }
	            }
	          }
	        );
	      });
	    }

	    this.show = function(missionid) {
	      var self = this;
	      makeCall("GET", "GetMissionDetailsData?missionid=" + missionid, null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var mission = JSON.parse(req.responseText);
	              self.update(mission); // self is the object on which the function
	              // is applied
	              self.detailcontainer.style.visibility = "visible";
	              switch (mission.status) {
	                case "OPEN":
	                  self.expensecontainer.style.visibility = "hidden";
	                  self.expenseform.style.visibility = "visible";
	                  self.expenseform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "hidden";
	                  break;
	                case "REPORTED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "visible";
	                  break;
	                case "CLOSED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.style.visibility = "hidden";
	                  break;
	              }
	            } else if (req.status == 403) {
                  window.location.href = req.getResponseHeader("Location");
                  window.sessionStorage.removeItem('username');
                  }
                  else {
	              self.alert.textContent = message;

	            }
	          }
	        }
	      );
	    };

	    this.reset = function() {
	      this.detailcontainer.style.visibility = "hidden";
	      this.expensecontainer.style.visibility = "hidden";
	      this.expenseform.style.visibility = "hidden";
	      this.closeform.style.visibility = "hidden";
	    }

	    this.update = function(m) {
	      this.date.textContent = m.startDate;
	      this.destination.textContent = m.destination;
	      this.status.textContent = m.status;
	      this.description.textContent = m.description;
	      this.country.textContent = m.country;
	      this.province.textContent = m.province;
	      this.city.textContent = m.city;
	      this.fund.textContent = m.fund;
	      this.food.textContent = m.expenses.food;
	      this.accomodation.textContent = m.expenses.accomodation;
	      this.travel.textContent = m.expenses.transportation;
	    }
	  }

	  function Wizard(wizardId, alert) {
	    // minimum date the user can choose, in this case now and in the future
	    var now = new Date(),
	      formattedDate = now.toISOString().substring(0, 10);
	    this.wizard = wizardId;
	    this.alert = alert;

	    this.wizard.querySelector('input[type="date"]').setAttribute("min", formattedDate);

	    this.registerEvents = function(orchestrator) {
	      // Manage previous and next buttons
	      Array.from(this.wizard.querySelectorAll("input[type='button'].next,  input[type='button'].prev")).forEach(b => {
	        b.addEventListener("click", (e) => { // arrow function preserve the
	          // visibility of this
	          var eventfieldset = e.target.closest("fieldset"),
	            valid = true;
	          if (e.target.className == "next") {
	            for (i = 0; i < eventfieldset.elements.length; i++) {
	              if (!eventfieldset.elements[i].checkValidity()) {
	                eventfieldset.elements[i].reportValidity();
	                valid = false;
	                break;
	              }
	            }
	          }
	          if (valid) {
	            this.changeStep(e.target.parentNode, (e.target.className === "next") ? e.target.parentNode.nextElementSibling : e.target.parentNode.previousElementSibling);
	          }
	        }, false);
	      });

	      // Manage submit button
	      this.wizard.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
	        var eventfieldset = e.target.closest("fieldset"),
	          valid = true;
	        for (i = 0; i < eventfieldset.elements.length; i++) {
	          if (!eventfieldset.elements[i].checkValidity()) {
	            eventfieldset.elements[i].reportValidity();
	            valid = false;
	            break;
	          }
	        }

	        if (valid) {
	          var self = this;
	          makeCall("POST", 'CreateMission', e.target.closest("form"),
	            function(req) {
	              if (req.readyState == XMLHttpRequest.DONE) {
	                var message = req.responseText; // error message or mission id
	                if (req.status == 200) {
	                  orchestrator.refresh(message); // id of the new mission passed
	                } else if (req.status == 403) {
                      window.location.href = req.getResponseHeader("Location");
                      window.sessionStorage.removeItem('username');
                  }
                  else {
	                  self.alert.textContent = message;
	                  self.reset();
	                }
	              }
	            }
	          );
	        }
	      });
	      // Manage cancel button
	      this.wizard.querySelector("input[type='button'].cancel").addEventListener('click', (e) => {
	        e.target.closest('form').reset();
	        this.reset();
	      });
	    };

	    this.reset = function() {
	      var fieldsets = document.querySelectorAll("#" + this.wizard.id + " fieldset");
	      fieldsets[0].hidden = false;
	      fieldsets[1].hidden = true;
	      fieldsets[2].hidden = true;

	    }

	    this.changeStep = function(origin, destination) {
	      origin.hidden = true;
	      destination.hidden = false;
	    }
	  }

	  function PageOrchestrator() {
	    var alertContainer = document.getElementById("id_alert");
	    
	    this.start = function() {
	      personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
	        document.getElementById("id_username"));
	      personalMessage.show();

	      missionsList = new MissionsList(
	        alertContainer,
	        document.getElementById("id_listcontainer"),
	        document.getElementById("id_listcontainerbody"));

	      missionDetails = new MissionDetails({ // many parameters, wrap them in an
	        // object
	        alert: alertContainer,
	        detailcontainer: document.getElementById("id_detailcontainer"),
	        expensecontainer: document.getElementById("id_expensecontainer"),
	        expenseform: document.getElementById("id_expenseform"),
	        closeform: document.getElementById("id_closeform"),
	        date: document.getElementById("id_date"),
	        destination: document.getElementById("id_destination"),
	        status: document.getElementById("id_status"),
	        description: document.getElementById("id_description"),
	        country: document.getElementById("id_country"),
	        province: document.getElementById("id_province"),
	        city: document.getElementById("id_city"),
	        fund: document.getElementById("id_fund"),
	        food: document.getElementById("id_food"),
	        accomodation: document.getElementById("id_accomodation"),
	        transportation: document.getElementById("id_transportation")
	      });
	      missionDetails.registerEvents(this); // the orchestrator passes itself --this-- so that the wizard can call its refresh function after updating a mission

	      wizard = new Wizard(document.getElementById("id_createmissionform"), alertContainer);
	      wizard.registerEvents(this);  // the orchestrator passes itself --this-- so that the wizard can call its refresh function after creating a mission

	      document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })
	    };

	    this.refresh = function(currentMission) { // currentMission initially null at start
	      alertContainer.textContent = "";        // not null after creation of status change
	      missionsList.reset();
	      missionDetails.reset();
	      missionsList.show(function() {
	        missionsList.autoclick(currentMission); 
	      }); // closure preserves visibility of this
	      wizard.reset();
	    };
	  }
	};
