package de.maxkroner.database;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Optional;

import org.junit.Test;

public class BotDatabaseTest {
	private BotDatabase db = new BotDatabase("botdbtest");

	@Test
	public void createDatabaseTables() throws SQLException {
		// GIVEN
		db.resetDatabase();

		// THEN
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'GUILDPROPERTIESSTRING'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'GUILDPROPERTIESBOOLEAN'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'GUILDPROPERTIESINT'"), is(1));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesstring"), is(0));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesboolean"), is(0));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesint"), is(0));
	}
	
	@Test
	public void addGetGuildStringProperty() throws SQLException {
		// GIVEN
		db.resetDatabase();
		
		// WHEN
		db.addGuildProperty(1, "name", "guild1");
		db.addGuildProperty(1, "desc", "desc1");
		db.addGuildProperty(2, "name", "guild2");
		db.addGuildProperty(3, "desc", "desc3");

		// THEN
		assertThat(db.getStringGuildProperty(1, "name").orElse(null), is("guild1"));
		assertThat(db.getStringGuildProperty(1, "desc").orElse(null), is("desc1"));
		assertThat(db.getStringGuildProperty(2, "name").orElse(null), is("guild2"));
		assertThat(db.getStringGuildProperty(3, "desc").orElse(null), is("desc3"));
		assertThat(db.getStringGuildProperty(2, "desc"), is(Optional.empty()));
		assertThat(db.getStringGuildProperty(3, "name"), is(Optional.empty()));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesstring"), is(4));
		
		//WHEN
		db.addGuildProperty(1, "name", "guild2");
		db.addGuildProperty(2, "name", "guild1");
		
		// THEN
		assertThat(db.getStringGuildProperty(1, "name").orElse(null), is("guild2"));
		assertThat(db.getStringGuildProperty(2, "name").orElse(null), is("guild1"));	
	}
	
	@Test
	public void addGetGuildBooleanProperty() throws SQLException {
		// GIVEN
		db.resetDatabase();
		
		// WHEN
		db.addGuildProperty(1, "hasA", true);
		db.addGuildProperty(1, "hasB", false);
		db.addGuildProperty(2, "hasA", true);
		db.addGuildProperty(3, "hasB", false);

		// THEN
		assertThat(db.getBooleanGuildProperty(1, "hasA").orElse(null), is(true));
		assertThat(db.getBooleanGuildProperty(1, "hasB").orElse(null), is(false));
		assertThat(db.getBooleanGuildProperty(2, "hasA").orElse(null), is(true));
		assertThat(db.getBooleanGuildProperty(3, "hasB").orElse(null), is(false));
		assertThat(db.getBooleanGuildProperty(2, "hasB"), is(Optional.empty()));
		assertThat(db.getBooleanGuildProperty(3, "hasA"), is(Optional.empty()));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesboolean"), is(4));
		
		db.addGuildProperty(1, "hasA", false);
		db.addGuildProperty(1, "hasB", true);
		
		assertThat(db.getBooleanGuildProperty(1, "hasA").orElse(null), is(false));
		assertThat(db.getBooleanGuildProperty(1, "hasB").orElse(null), is(true));
	}
	
	@Test
	public void addGetGuildIntProperty() throws SQLException {
		// GIVEN
		db.resetDatabase();
		
		// WHEN
		db.addGuildProperty(1, "number1", 1);
		db.addGuildProperty(1, "number2", 2);
		db.addGuildProperty(2, "number1", 1);
		db.addGuildProperty(3, "number2", 2);

		// THEN
		assertThat(db.getIntGuildProperty(1, "number1").orElse(null), is(1));
		assertThat(db.getIntGuildProperty(1, "number2").orElse(null), is(2));
		assertThat(db.getIntGuildProperty(2, "number1").orElse(null), is(1));
		assertThat(db.getIntGuildProperty(3, "number2").orElse(null), is(2));
		assertThat(db.getIntGuildProperty(2, "number2"), is(Optional.empty()));
		assertThat(db.getIntGuildProperty(3, "number1"), is(Optional.empty()));
		assertThat(db.getRowCount("SELECT COUNT(*) FROM guildpropertiesint"), is(4));
		
		//WHEN
		db.addGuildProperty(1, "number1", 2);
		db.addGuildProperty(2, "number1", 2);
				
		// THEN
		assertThat(db.getIntGuildProperty(1, "number1").orElse(null), is(2));
		assertThat(db.getIntGuildProperty(2, "number1").orElse(null), is(2));	
	}

}
