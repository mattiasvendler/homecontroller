package se.vendler;

import org.sql2o.Connection;
import org.sql2o.Query;

public class HumidityWriter extends DataWriter {
	@Override
	protected Query getQuery(DataWrite humidityWrite, Connection open) {
			return open.createQuery("insert into environment.humidity (node_id, humidity) values (:node_id, :data);")
					       .addParameter("node_id", humidityWrite.getNodeId())
					       .addParameter("data", humidityWrite.getData());
	}
}
