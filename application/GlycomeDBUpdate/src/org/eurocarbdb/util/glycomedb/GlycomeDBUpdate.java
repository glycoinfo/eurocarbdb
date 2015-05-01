package org.eurocarbdb.util.glycomedb;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jibx.runtime.JiBXException;


public class GlycomeDBUpdate {
	
	public static void main(String[] args) {
		
		if(args.length == 0){
			System.err.println("No information of property.ini file");
			System.exit(1);
		}
		// load configuration from ini
		Configuration m_config;
		try {
			m_config = new Configuration(args[0]);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
			System.out.println("Failed");
			return;
		} catch (IOException e) {
			e.printStackTrace(System.out);
			System.out.println("Failed");
			return;
		}
		
		//create a class for downloading, parsing and database storage
		GlycomeDBUpdateUtil t_util = new GlycomeDBUpdateUtil(m_config);
		try {
			t_util.performUpdate();
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
			System.out.println("Failed");
			return;
		} catch (JiBXException e) {
			e.printStackTrace(System.out);
			System.out.println("Failed");
			return;
		}
		//updating database
		UpdatingEuroCarbDataBase updateEuroData = new UpdatingEuroCarbDataBase();
		updateEuroData.startLoading(t_util.getDataExport(),m_config);
		System.out.println("Successful");
	}
}
