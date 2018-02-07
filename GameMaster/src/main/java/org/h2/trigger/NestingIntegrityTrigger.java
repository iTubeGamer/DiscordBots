package org.h2.trigger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.api.Trigger;
import org.h2.tools.TriggerAdapter;

import de.maxkroner.values.Values;

public class NestingIntegrityTrigger extends TriggerAdapter implements Trigger{

	@Override
	public void fire(Connection conn, ResultSet oldRow, ResultSet newRow) throws SQLException {
		if(conn != null && newRow != null){
			int outer_list_id = newRow.getInt(2);
			int inner_list_id = newRow.getInt(3);
			
			String query = "SELECT COUNT(*) FROM listnesting WHERE inner_list_id=" + outer_list_id + " AND outer_list_id=" + inner_list_id;
			ResultSet rs = conn.createStatement().executeQuery(query);
			rs.next();
			int integrityViolationCount = rs.getInt(1);
			
			if(integrityViolationCount != 0){
				throw new SQLException(Values.MESSAGE_SQL_ERROR_NESTING);
			}
		}		
	}
}
