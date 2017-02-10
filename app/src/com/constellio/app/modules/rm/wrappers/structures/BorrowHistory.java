package com.constellio.app.modules.rm.wrappers.structures;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.utils.Lazy;

public class BorrowHistory implements ModifiableStructure {

	private static final long serialVersionUID = -4989780953233425302L;

	public static final String DATE_FORMAT = "yyyyMMddHHmmssSSSXXX";

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);

	private String id;
	private String borrowerId;
	private String borrowerUserName;
	private String approvingId;
	private String approvingUserName;
	private Date planifiedReturnDate;
	private Date borrowDate;
	private Date returnDate;
	private boolean dirty;

	private Lazy<List<BorrowHistory>> lazyHistory;

	private List<BorrowHistory> history;

	public BorrowHistory() {
	}

	public BorrowHistory(String id, String borrowerId, String borrowerUserName, String approvingId,
			String approvingUserName, Date planifiedReturnDate, Date borrowDate, Date returnDate) {
		this.id = id;
		this.borrowerId = borrowerId;
		this.borrowerUserName = borrowerUserName;
		this.approvingId = approvingId;
		this.approvingUserName = approvingUserName;
		this.planifiedReturnDate = planifiedReturnDate;
		this.borrowDate = borrowDate;
		this.returnDate = returnDate;
	}

	protected BorrowHistory(String id, String borrowerId, String borrowerUserName, String approvingId,
			String approvingUserName, String planifiedReturnDate, String borrowDate, String returnDate) {
		this(id, borrowerId, borrowerUserName, approvingId, approvingUserName, parseDate(planifiedReturnDate),
				parseDate(borrowDate), parseDate(returnDate));
	}

	protected void setLazyHistory(Lazy<List<BorrowHistory>> lazyHistory) {
		this.lazyHistory = lazyHistory;
	}

	public BorrowHistory setId(String id) {
		this.dirty = true;
		this.id = id;
		return this;
	}

	protected BorrowHistory setApprovingId(String approvingId) {
		this.dirty = true;
		this.approvingId = approvingId;
		return this;
	}

	protected BorrowHistory setApprovingUserName(String approvingUserName) {
		this.dirty = true;
		this.approvingUserName = approvingUserName;
		return this;
	}

	protected BorrowHistory setBorrowDate(Date borrowDate) {
		this.dirty = true;
		this.borrowDate = borrowDate;
		return this;
	}

	protected BorrowHistory setBorrowerId(String borrowerId) {
		this.dirty = true;
		this.borrowerId = borrowerId;
		return this;
	}

	protected BorrowHistory setBorrowerUserName(String borrowerUserName) {
		this.dirty = true;
		this.borrowerUserName = borrowerUserName;
		return this;
	}

	protected BorrowHistory setPlanifiedReturnDate(Date planifiedReturnDate) {
		this.dirty = true;
		this.planifiedReturnDate = planifiedReturnDate;
		return this;
	}

	protected BorrowHistory setReturnDate(Date returnDate) {
		this.dirty = true;
		this.returnDate = returnDate;
		return this;
	}

	public String getId() {
		return id;
	}

	public String getApprovingId() {
		return approvingId;
	}

	public String getApprovingUserName() {
		return approvingUserName;
	}

	public Date getBorrowDate() {
		return borrowDate;
	}

	public String getBorrowDateFormatted() {
		return formatDate(borrowDate);
	}

	public String getBorrowerId() {
		return borrowerId;
	}

	public String getBorrowerUserName() {
		return borrowerUserName;
	}

	public Date getPlanifiedReturnDate() {
		return planifiedReturnDate;
	}

	public String getPlanifiedReturnDateFormatted() {
		return formatDate(planifiedReturnDate);
	}

	public Date getReturnDate() {
		return returnDate;
	}

	public String getReturnDateFormatted() {
		return formatDate(returnDate);
	}

	private List<BorrowHistory> initHistory() {
		if (history == null) {
			history = new ArrayList<BorrowHistory>();

			if (lazyHistory != null) {
				history.addAll(lazyHistory.get());
			}
		}

		return history;
	}

	@SuppressWarnings("unchecked")
	public List<BorrowHistory> getHistory() {
		return ListUtils.unmodifiableList(initHistory());
	}

	// TODO : ajouter une méthode qui permet d'emprunter et pusher dans history
	// TODO : ajouter une méthode qui permet de faire un retour (date de retour
	// - vider la date de planified)
	// Changer attribut planifiedReturnDate en planifiedReturnDate
	protected void pushToHistory() {
		List<BorrowHistory> his = initHistory();
		boolean notValid = !his.isEmpty() && equalsWithoutHistory(his.get(0));

		if (!notValid) {
			his.add(new BorrowHistory(id, borrowerId, borrowerUserName, approvingId, approvingUserName,
					planifiedReturnDate, borrowDate, returnDate));
		}
	}

	public void emprunter(String borrowerId, String borrowerUserName, String approvingId, String approvingUserName,
			Date planifiedReturnDate, Date borrowDate) {
		pushToHistory();

		this.borrowerId = borrowerId;
		this.borrowerUserName = borrowerUserName;
		this.approvingId = approvingId;
		this.approvingUserName = approvingUserName;
		this.planifiedReturnDate = planifiedReturnDate;
		this.borrowDate = borrowDate;
	}

	public void retourner(Date returnDate) {
		this.planifiedReturnDate = null;
		this.returnDate = returnDate;
	}

	protected boolean equalsWithoutHistory(BorrowHistory borrowHistory) {
		return EqualsBuilder.reflectionEquals(this, borrowHistory, "dirty", "lazyHistory", "history");
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	public static String formatDate(Date date) {
		if (date != null) {
			return DATE_FORMATTER.format(date);
		} else {
			return null;
		}
	}

	public static Date parseDate(String date) {
		try {
			if (date != null) {
				return DATE_FORMATTER.parse(date);
			}
		} catch (ParseException e) {
		}

		return null;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, "dirty", "lazyHistory");
	}
}
