package se.vendler;

import org.sql2o.Connection;
import org.sql2o.Query;

public class TemperatureWriter extends DataWriter {
	@Override
	protected Query getQuery(DataWrite humidityWrite, Connection open) {
		return open.createQuery("insert into environment.temperature (node_id, temperature) values (:node_id, :data);")
				       .addParameter("node_id", humidityWrite.getNodeId())
				       .addParameter("data", humidityWrite.getData());
		
	}
}
