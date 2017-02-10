package com.constellio.app.modules.rm.wrappers.structures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class BorrowHistoryFactoryAcceptanceTest extends ConstellioTest {

	private static final Date RETURN_DATE = new Date();
	private static final Date PLANIFIED_RETURN_DATE = RETURN_DATE;
	private static final String BORROWER_USER_NAME = "borrowerUserName";
	private static final String BORROWER_ID = "borrowerId";
	private static final Date BORROW_DATE = PLANIFIED_RETURN_DATE;
	private static final String APPROVING_USER_NAME = "approvingUserName";
	private static final String APPROVING_ID = "approvingId";

	private BorrowHistoryFactory factory;

	public BorrowHistoryFactoryAcceptanceTest() {
	}

	@Before
	public void setUp() throws Exception {
		factory = new BorrowHistoryFactory();
	}

	@Test
	public void whenSetAttributeValueThenBecomesDirtyAndValueSet() {
		BorrowHistory borrowHistory = new BorrowHistory();
		assertThat(borrowHistory.isDirty()).isFalse();

		borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isEqualTo(APPROVING_ID);
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setApprovingUserName(APPROVING_USER_NAME);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isEqualTo(APPROVING_USER_NAME);
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setBorrowDate(BORROW_DATE);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isEqualTo(BORROW_DATE);
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setBorrowerId(BORROWER_ID);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isEqualTo(BORROWER_ID);
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setBorrowerUserName(BORROWER_USER_NAME);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isEqualTo(BORROWER_USER_NAME);
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setPlanifiedReturnDate(PLANIFIED_RETURN_DATE);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isEqualTo(PLANIFIED_RETURN_DATE);
		assertThat(borrowHistory.getReturnDate()).isNull();

		borrowHistory = new BorrowHistory().setReturnDate(RETURN_DATE);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isNull();
		assertThat(borrowHistory.getApprovingUserName()).isNull();
		assertThat(borrowHistory.getBorrowDate()).isNull();
		assertThat(borrowHistory.getBorrowerId()).isNull();
		assertThat(borrowHistory.getBorrowerUserName()).isNull();
		assertThat(borrowHistory.getPlanifiedReturnDate()).isNull();
		assertThat(borrowHistory.getReturnDate()).isEqualTo(RETURN_DATE);

		borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID).setApprovingUserName(APPROVING_USER_NAME)
				.setBorrowDate(BORROW_DATE).setBorrowerId(BORROWER_ID).setBorrowerUserName(BORROWER_USER_NAME)
				.setPlanifiedReturnDate(PLANIFIED_RETURN_DATE).setReturnDate(RETURN_DATE);
		assertThat(borrowHistory.isDirty()).isTrue();
		assertThat(borrowHistory.getApprovingId()).isEqualTo(APPROVING_ID);
		assertThat(borrowHistory.getApprovingUserName()).isEqualTo(APPROVING_USER_NAME);
		assertThat(borrowHistory.getBorrowDate()).isEqualTo(BORROW_DATE);
		assertThat(borrowHistory.getBorrowerId()).isEqualTo(BORROWER_ID);
		assertThat(borrowHistory.getBorrowerUserName()).isEqualTo(BORROWER_USER_NAME);
		assertThat(borrowHistory.getPlanifiedReturnDate()).isEqualTo(PLANIFIED_RETURN_DATE);
		assertThat(borrowHistory.getReturnDate()).isEqualTo(RETURN_DATE);
	}

	@Test
	public void whenConvertingStructureWithAllValuesThenRemainsEqual() {
		BorrowHistory borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID)
				.setApprovingUserName(APPROVING_USER_NAME).setBorrowDate(BORROW_DATE).setBorrowerId(BORROWER_ID)
				.setBorrowerUserName(BORROWER_USER_NAME).setPlanifiedReturnDate(PLANIFIED_RETURN_DATE)
				.setReturnDate(RETURN_DATE);
		
		String toString = factory.toString(borrowHistory);
		assertThat(toString).isNotEmpty();
		
		BorrowHistory build = factory.build(toString);
		assertThat(build).isEqualTo(borrowHistory);
		
		String toString2 = factory.toString(build);
		assertThat(toString2).isEqualTo(toString);
	}

	@Test
	public void whenConvertingStructureWithNullValuesThenRemainsEqual() {
		BorrowHistory borrowHistory = new BorrowHistory();
		
		String toString = factory.toString(borrowHistory);
		assertThat(toString).isNotEmpty();
		
		BorrowHistory build = factory.build(toString);
		assertThat(build).isEqualTo(borrowHistory);
		
		String toString2 = factory.toString(build);
		assertThat(toString2).isEqualTo(toString);
	}

	@Test
	public void whenHavingHistoryThenRemainsEqual() {
		BorrowHistory borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID)
				.setApprovingUserName(APPROVING_USER_NAME).setBorrowDate(BORROW_DATE).setBorrowerId(BORROWER_ID)
				.setBorrowerUserName(BORROWER_USER_NAME).setPlanifiedReturnDate(PLANIFIED_RETURN_DATE)
				.setReturnDate(RETURN_DATE);
		
		borrowHistory.pushToHistory();
		assertThat(borrowHistory.getHistory()).isNotEmpty().hasSize(1);
		
		borrowHistory.setBorrowerUserName("changedUserName");
		
		String toString = factory.toString(borrowHistory);
		assertThat(toString).isNotEmpty();
		
		BorrowHistory build = factory.build(toString);
		assertThat(build.getHistory()).isNotEmpty().hasSize(1);
		assertThat(build).isEqualTo(borrowHistory);
	}

	@Test
	public void currentCanNotBePushedTwiceInHistoryWithoutNewModification() {
		BorrowHistory borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID)
				.setApprovingUserName(APPROVING_USER_NAME).setBorrowDate(BORROW_DATE).setBorrowerId(BORROWER_ID)
				.setBorrowerUserName(BORROWER_USER_NAME).setPlanifiedReturnDate(PLANIFIED_RETURN_DATE)
				.setReturnDate(RETURN_DATE);
		
		borrowHistory.pushToHistory();
		assertThat(borrowHistory.getHistory()).isNotEmpty().hasSize(1);
		
		borrowHistory.pushToHistory();
		assertThat(borrowHistory.getHistory()).isNotEmpty().hasSize(1);
		
		borrowHistory.setBorrowerUserName("changedUserName");
		borrowHistory.pushToHistory();
		assertThat(borrowHistory.getHistory()).isNotEmpty().hasSize(2);
	}

	@Test
	public void whenPushedToHistoryAndNeverChangedNoMoreHistoryIsSerialized() {
		BorrowHistory borrowHistory = new BorrowHistory().setApprovingId(APPROVING_ID)
				.setApprovingUserName(APPROVING_USER_NAME).setBorrowDate(BORROW_DATE).setBorrowerId(BORROWER_ID)
				.setBorrowerUserName(BORROWER_USER_NAME).setPlanifiedReturnDate(PLANIFIED_RETURN_DATE)
				.setReturnDate(RETURN_DATE);
		
		borrowHistory.pushToHistory();
		assertThat(borrowHistory.getHistory()).isNotEmpty().hasSize(1);
		
		String toString = factory.toString(borrowHistory);
		assertThat(toString).isNotEmpty();
		
		BorrowHistory build = factory.build(toString);
		assertThat(build).isNotEqualTo(borrowHistory);
		
		assertThat(build.getHistory()).isEmpty();
		assertThat(build.equalsWithoutHistory(borrowHistory)).isTrue();
	}
}
