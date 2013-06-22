package lempel.blueprint.service.socks;

import java.io.IOException;
import java.util.Vector;

import lempel.blueprint.concurrent.Worker;

/**
 * Relay Reactor를 Target으로 연결하는 Worker
 * 
 * @author Simon Lee
 * @version $Revision$
 * @create 2007. 12. 12
 * @since 1.5
 * @last $Date$
 * @see
 */
public class ConnectorWorker extends Worker {
	/**
	 * Constructor
	 * 
	 * @param jobQueue
	 * @param sleepingWorkers
	 */
	public ConnectorWorker(Vector<Object> jobQueue, Vector<Worker> sleepingWorkers) {
		super(jobQueue, sleepingWorkers);
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.concurrent.Worker#process(java.lang.Object)
	 */
	protected void process(Object client) {
		ConnectionInfo info = (ConnectionInfo) client;

		Socks5RelayReactor relayReactor = info.getRelayReactor();
		Socks5Reactor reactor = relayReactor.getPartner();
		ServerResponse serverReqPacket = reactor.getServerResponsePacket();

		// 접속 결과를 client에게 마지막 응답 packet으로 전송
		if (!relayReactor.connect(info.getAddress(), info.getPort(), info
				.getSelector())) {
			// host unreachable
			serverReqPacket.setRep((byte) 0x04);
			try {
				reactor.sendRawData(serverReqPacket.serialize());
			} catch (IOException ignored) {
			}
			relayReactor.terminate();
		} else {
			// success
			serverReqPacket.setRep((byte) 0x00);
			try {
				reactor.sendRawData(serverReqPacket.serialize());
			} catch (IOException e) {
				reactor.terminate();
			}
		}
	}

	/*
	 * (non-Javadoc, override method)
	 * 
	 * @see lempel.blueprint.terminator.Terminatable#terminate()
	 */
	public void terminate() {
		setRunning(false);
	}
}
