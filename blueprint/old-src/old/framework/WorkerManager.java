package lempel.old.framework;

import java.util.*;


/**
 * Worker들을 관리
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2005.1.4.
 */
public class WorkerManager
	extends Thread
{
	/** 전체 Worker들을 보관 */
	protected Vector _activeWorkers = null;

	/** 대기상태의 Worker들을 보관 */
	protected Vector _waitingWorkers = null;

	/** 최대 부하 (처리량/sec) */
	protected int _maxLoad = 0;

	/** 부하 체크 간격 (sec) */
	protected int _interval = 1;

	/** 부하 조절용 delay 값 (msec) */
	protected int _delay = 0;

	/** 현재 부하 (처리량) */
	protected int _currentLoad = 0;

	/** Thread 실행 지속 여부 */
	protected boolean _runFlag = true;

	public WorkerManager()
	{
		_activeWorkers = new Vector();
		_waitingWorkers = new Vector();
	}

	public void run()
	{
		if (get_maxLoad() <= 0)
			_runFlag = false;

		while(_runFlag)
		{
			try
			{
				// 체크 주기만큼 대기
				sleep(get_interval() * 1000);

				int currentLoad = get_currentLoad();
				_currentLoad = 0;
				// 현재 부하량을 기준으로 현재의 초당 처리량 계산
				int tps = currentLoad / get_interval();
				// 제한 속도와의 차이를 계싼
				int diffLoad = tps - get_maxLoad();
				// 제한 속도를 초과한 경우
				if (diffLoad > 0)
					// 속도 조절을 위해 필요한 딜레이를 계산
					set_delay(diffLoad / _activeWorkers.size() / get_delay());
				else
					set_delay(0);
			}
			catch(InterruptedException ex)
			{}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Graceful한 Thread 종료를 유도
	 */
	public void terminate()
	{
		_runFlag = false;
	}

	/**
	 * 대기상태에 있는 Worker 하나를 활성화시킨다
	 */
	public void notifyWorker()
	{
		Object aWorker = null;

		synchronized(_waitingWorkers)
		{
			if (_waitingWorkers.size() > 0)
				aWorker = _waitingWorkers.remove(0);
		}

		if (aWorker != null)
		{
			synchronized(aWorker)
			{
				aWorker.notify();
			}
		}
	}

	/**
	 * 대기상태에 들어간 Worker를 등록
	 * 
	 * @param aWorker
	 *          대기상태의 Worker 객체
	 */
	public void addWorker(Worker aWorker)
	{
		synchronized(_waitingWorkers)
		{
			_activeWorkers.addElement(aWorker);
		}
	}

	/**
	 * 대기상태에 들어간 Worker를 등록
	 * 
	 * @param aWorker
	 *          대기상태의 Worker 객체
	 */
	public void addWaitingWorker(Worker aWorker)
	{
		synchronized(_waitingWorkers)
		{
			_waitingWorkers.addElement(aWorker);
		}
	}

	/**
	 * 전체 Worker의 수를 반환
	 * 
	 * @return 전체 Worker의 수
	 */
	public int size()
	{
		synchronized(_activeWorkers)
		{
			return _activeWorkers.size();
		}
	}

	/**
	 * @return Returns the _delay.
	 */
	public synchronized int get_delay()
	{
		return _delay;
	}

	/**
	 * @param _delay
	 *          The _delay to set.
	 */
	public synchronized void set_delay(int _delay)
	{
		this._delay = _delay;
	}

	/**
	 * @return Returns the _interval.
	 */
	public synchronized int get_interval()
	{
		return _interval;
	}

	/**
	 * @param _interval
	 *          The _interval to set.
	 */
	public synchronized void set_interval(int _interval)
	{
		this._interval = _interval;
	}

	/**
	 * @return Returns the _maxLoad.
	 */
	public synchronized int get_maxLoad()
	{
		return _maxLoad;
	}

	/**
	 * @param load
	 *          The _maxLoad to set.
	 */
	public synchronized void set_maxLoad(int load)
	{
		_maxLoad = load;
	}

	/**
	 * @return Returns the _currentLoad.
	 */
	public synchronized int get_currentLoad()
	{
		return _currentLoad;
	}

	/**
	 * 부하 카운터를 1 증가
	 */
	public synchronized void add_currentLoad()
	{
		_currentLoad++;
	}
}