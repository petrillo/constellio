package com.constellio.app.modules.rm.wrappers.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.utils.Lazy;

public class BorrowHistoryFactory implements StructureFactory<BorrowHistory> {

	private static final long serialVersionUID = 3605511696489450320L;

	private static final String FIELD_SEPARATOR = "|";
	private static final String BORROW_HISTORY_SEPARATOR = "\uc2bfBH\uc2bf";

	private static final String NULL_VALUE = "~~NULL~~";

	private static String BORROW_HISTORY_ID = "id=";
	private static String BORROWER_ID = "bi=";
	private static String BORROWER_USER_NAME = "bun=";
	private static String APPROVING_ID = "ai=";
	private static String APPROVING_USER_NAME = "aun=";
	private static String PLANIFIED_RETURN_DATE = "prd=";
	private static String BORROW_DATE = "bd=";
	private static String RETURN_DATE = "rd=";

	public BorrowHistoryFactory() {
	}

	@Override
	public BorrowHistory build(String string) {
		final StringTokenizer st = new StringTokenizer(string, BORROW_HISTORY_SEPARATOR);

		BorrowHistory bh = null;

		if (st.hasMoreTokens()) {
			bh = buildBorrowHistory(st.nextToken());

			bh.setLazyHistory(new Lazy<List<BorrowHistory>>() {
				@Override
				@SuppressWarnings("unchecked")
				protected List<BorrowHistory> load() {
					ArrayList<BorrowHistory> list = new ArrayList<BorrowHistory>();

					while (st.hasMoreElements()) {
						list.add(buildBorrowHistory(st.nextToken()));
					}

					return ListUtils.unmodifiableList(list);
				}
			});
		}

		return bh;
	}

	private BorrowHistory buildBorrowHistory(String string) {
		StringTokenizer st = new StringTokenizer(string, FIELD_SEPARATOR);

		BorrowHistory bh = new BorrowHistory(
				extractValue(BORROW_HISTORY_ID, st.nextToken()),
				extractValue(BORROWER_ID, st.nextToken()),
				extractValue(BORROWER_USER_NAME, st.nextToken()),
				extractValue(APPROVING_ID, st.nextToken()),
				extractValue(APPROVING_USER_NAME, st.nextToken()),
				extractValue(PLANIFIED_RETURN_DATE, st.nextToken()),
				extractValue(BORROW_DATE, st.nextToken()),
				extractValue(RETURN_DATE, st.nextToken())
		);
		
		return bh;
	}

	@Override
	public String toString(BorrowHistory bh) {
		StringBuilder sb = new StringBuilder(toStringBorrowHistory(bh));

		List<BorrowHistory> histories = bh.getHistory();
		for (int i = 0; i < histories.size(); i++) {
			BorrowHistory borrowHistory = histories.get(i);
			boolean notValid = (i == 0 && bh.equalsWithoutHistory(borrowHistory));

			if (!notValid) {
				sb.append(BORROW_HISTORY_SEPARATOR).append(toStringBorrowHistory(borrowHistory));
			}
		}

		return sb.toString();
	}

	private String toStringBorrowHistory(BorrowHistory bh) {
		StringBuilder sb = new StringBuilder();

		sb.append(encode(BORROW_HISTORY_ID, bh.getId())).append(FIELD_SEPARATOR);
		sb.append(encode(BORROWER_ID, bh.getBorrowerId())).append(FIELD_SEPARATOR);
		sb.append(encode(BORROWER_USER_NAME, bh.getBorrowerUserName())).append(FIELD_SEPARATOR);
		sb.append(encode(APPROVING_ID, bh.getApprovingId())).append(FIELD_SEPARATOR);
		sb.append(encode(APPROVING_USER_NAME, bh.getApprovingUserName())).append(FIELD_SEPARATOR);
		sb.append(encode(PLANIFIED_RETURN_DATE, bh.getPlanifiedReturnDateFormatted())).append(FIELD_SEPARATOR);
		sb.append(encode(BORROW_DATE, bh.getBorrowDateFormatted())).append(FIELD_SEPARATOR);
		sb.append(encode(RETURN_DATE, bh.getReturnDateFormatted()));

		return sb.toString();
	}

	private String extractValue(String fieldCode, String from) {
		String value = StringUtils.removeStart(from, fieldCode);

		if (NULL_VALUE.equals(value)) {
			return null;
		} else {
			return value;
		}
	}

	private String encode(String fieldCode, String value) {
		if (StringUtils.isBlank(value)) {
			value = NULL_VALUE;
		}

		return new StringBuilder(fieldCode).append(value).toString();
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
