package com.application.demo;
import java.util.ArrayList;

import com.fxcore2.*;

public class Account {
	private O2GSession session;

	public Account(O2GSession session) {
		this.session = session;
	}
	// Get the list of all accounts associated with the user
	public ArrayList<O2GAccountRow> getAccounts() throws Exception{
		ArrayList<O2GAccountRow> accounts = new ArrayList<>();
		O2GLoginRules loginRules = session.getLoginRules();
		O2GResponse response = loginRules.getTableRefreshResponse(O2GTableType.ACCOUNTS);
		O2GResponseReaderFactory readerFactory = session.getResponseReaderFactory();
		O2GAccountsTableResponseReader accountsResponseReader = readerFactory.createAccountsTableReader(response);
		// Add accounts to the list
		for (int i = 0; i < accountsResponseReader.size(); i++) {
			accounts.add(accountsResponseReader.getRow(i));
			}
		if(accounts.size() == 0){
			throw new Exception("No account asscoiated with the username");
		}
		return accounts;
	}
}
