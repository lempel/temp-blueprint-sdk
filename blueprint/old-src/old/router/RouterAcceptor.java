package lempel.old.router;

import java.io.*;
import java.net.*;

import lempel.blueprint.log.*;
import lempel.old.framework.*;


/**
 * Client의 연결 요청을 처리
 * 
 * @author Sang-min Lee
 * @since 2005.5.20.
 * @version 2005.5.20.
 */
public class RouterAcceptor extends Acceptor {

	protected Logger _log = null;

	protected FilteringServerSocket _ssock = null;

	public RouterAcceptor(XMLDocument doc) {
		super(doc);
	}

	protected boolean initResources() {
		_resources = new ResourceManager();

		// TODO: BluePrintContext를 이용하도록 수정!!

		XMLNode uplinkNode = _doc.getRootNode().getFirstChild("uplink");
		Uplink uplink = new Uplink();
		try {
			String ip = uplinkNode.getFirstChild("ip").getValue();
			int port = Integer.parseInt(uplinkNode.getFirstChild("port")
					.getValue());
			int timeout = Integer.parseInt(uplinkNode.getFirstChild("timeout")
					.getValue());
			uplink.connect(ip, port, timeout);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}

		_resources.addResource("Uplink", uplink);

		return true;
	}

	/**
	 * 주의! worker 수 만큼의 process들만 해당 router에 연결이 가능
	 */
	protected boolean initWorkers() {
		//	<?xml version="1.0" encoding="euc-kr" ?>
		//	<router>
		//	...
		//		<workers count="50"/>
		//	...
		//	</router>

		_workers = new WorkerManager();

		XMLNode workerNode = _doc.getRootNode().getFirstChild("workers");

		int workerCount = Integer.parseInt(workerNode.getAttribute("count"));
		Downlink worker = null;
		for (int i = 0; i < workerCount; i++) {
			worker = new Downlink(_workers, _jobQueue, workerNode, _resources);
			worker.start();
			_workers.addWaitingWorker(worker);
		}

		return true;
	}

	protected boolean initServerSocket() {
		//	<?xml version="1.0" encoding="euc-kr" ?>
		//	<router>
		//		...
		//		<serverInfo>
		//			<bindPort>10000</bindPort>
		//			<backlog>5</backlog>
		//			<bindIp>127.0.0.1</bindIp>
		//			<denyList>203.*.*.* 203.248.46.1</denyList>
		//			<allowList>127.0.0.1 192.168.1.*</allowList>
		//		</serverInfo>
		//		...
		//	</router>
		
		try {
			XMLNode serverInfo = _doc.getRootNode().getFirstChild("serverInfo");

			_log.println(LogLevel.INF, "Binding ServerSocket on (IP="
					+ serverInfo.getFirstChild("bindIp").getValue() + ", PORT="
					+ serverInfo.getFirstChild("bindPort").getValue()
					+ ", BACKLOG="
					+ serverInfo.getFirstChild("backLog").getValue() + ")");

			_ssock = FilteringServerSocket.create(serverInfo);
		} catch (Exception ex) {
			_log.println(LogLevel.ERR, _header + "Can't bind ServerSocket - "
					+ ex);
			ex.printStackTrace();

			return false;
		}

		return true;
	}

	protected Socket accept() throws IOException {
		return _ssock.accept();
	}

	public void run() {
		Socket _sock = null;
		while (_runFlag) {
			try {
				_sock = accept();

				if (_sock != null) {
					// 작업 queue에 추가
					_jobQueue.addElement(_sock);
					// worker 하나를 깨운다
					_workers.notifyWorker();
				}
			} catch (Throwable ex) {
				_log.println(LogLevel.WAN, "Can't accept client - " + ex);
				ex.printStackTrace();
			}
		}
	}
}