package org.mjd.repro.message.factory;

import java.io.ByteArrayInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.mjd.repro.message.RequestWithArgs;
import org.mjd.repro.util.kryo.KryoPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KryoRpcRequestMsgFactory<R extends RequestWithArgs> implements MessageFactory<R> {
	private static final Logger LOG = LoggerFactory.getLogger(KryoRpcRequestMsgFactory.class);

	private Kryo kryo;
	private KryoPool kryos;

	private final Class<R> type;

	public KryoRpcRequestMsgFactory(final Kryo kryo, final Class<R> type) {
		this.kryo = kryo;
		this.type = type;
	}

	public KryoRpcRequestMsgFactory(final KryoPool kryos, final Class<R> type) {
		this.kryos = kryos;
		this.type = type;
	}

	/**
	 * Expects a Kryo object with a marshalled RpcRequest
	 */
	@Override
	public R createMessage(final byte[] bytesRead) {
		final Kryo theKryo = this.kryo == null ? this.kryos.obtain() : this.kryo;
		try {
			return readBytesWithKryo(theKryo, bytesRead);
		}
		catch (final Exception e) {
			LOG.error("Exception", e);
			throw new MessageCreationException(e);
		}
		finally {
			if (this.kryo == null) {
				this.kryos.free(theKryo);
			}
		}
	}

	private R readBytesWithKryo(final Kryo kryo, final byte[] data) {
		try (ByteArrayInputStream bin = new ByteArrayInputStream(data); Input kryoByteArrayIn = new Input(bin)) {
			return kryo.readObject(kryoByteArrayIn, type);
		}
		catch (final Exception e) {
			LOG.error("Error deserialising response from server", e);
			throw new MessageCreationException(e);
		}
	}
}
