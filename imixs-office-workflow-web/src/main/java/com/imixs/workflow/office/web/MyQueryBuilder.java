package com.imixs.workflow.office.web;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Specializes;

import org.imixs.marty.view.QueryBuilder;
import org.imixs.workflow.ItemCollection;

@Alternative //@Specializes
@ApplicationScoped
public class MyQueryBuilder extends QueryBuilder {

	@Override
	public String getSearchQuery(ItemCollection searchFilter) {
		System.out.println("Ich bin drann... MyQuerybuilder");
		return super.getSearchQuery(searchFilter);
	}
	
	
	@Override
	public String getJPQLStatement(ItemCollection queryFilter) {
		System.out.println("Ich bin drann... MyQuerybuilder");
		return super.getJPQLStatement(queryFilter);
	}
}
