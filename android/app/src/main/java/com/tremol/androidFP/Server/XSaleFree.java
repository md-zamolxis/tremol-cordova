package com.tremol.androidFP.Server;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class XSaleFree {
	public static class Item {
		public String name;
		public char taxgrp;
		public float price;
		public float quantity;
		public float discountPCT;
	}

	public static class Payment {
		public String name;
		public Float amount;
	}


	@Attribute
	public String command;
	@ElementList(inline = true)
	public List<Item> items;

	@ElementList(inline = true)
	public List<Payment> payments;
}