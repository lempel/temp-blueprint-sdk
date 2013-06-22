package lempel.old.framework;

import java.util.*;


/**
 * Worker���� ����
 * 
 * @author Sang-min Lee
 * @since 2004.5.21.
 * @version 2005.1.4.
 */
public class WorkerManager
	extends Thread
{
	/** ��ü Worker���� ���� */
	protected Vector _activeWorkers = null;

	/** �������� Worker���� ���� */
	protected Vector _waitingWorkers = null;

	/** �ִ� ���� (ó����/sec) */
	protected int _maxLoad = 0;

	/** ���� üũ ���� (sec) */
	protected int _interval = 1;

	/** ���� ������ delay �� (msec) */
	protected int _delay = 0;

	/** ���� ���� (ó����) */
	protected int _currentLoad = 0;

	/** Thread ���� ���� ���� */
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
				// üũ �ֱ⸸ŭ ���
				sleep(get_interval() * 1000);

				int currentLoad = get_currentLoad();
				_currentLoad = 0;
				// ���� ���Ϸ��� �������� ������ �ʴ� ó���� ���
				int tps = currentLoad / get_interval();
				// ���� �ӵ����� ���̸� ���
				int diffLoad = tps - get_maxLoad();
				// ���� �ӵ��� �ʰ��� ���
				if (diffLoad > 0)
					// �ӵ� ������ ���� �ʿ��� �����̸� ���
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
	 * Graceful�� Thread ���Ḧ ����
	 */
	public void terminate()
	{
		_runFlag = false;
	}

	/**
	 * �����¿� �ִ� Worker �ϳ��� Ȱ��ȭ��Ų��
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
	 * �����¿� �� Worker�� ���
	 * 
	 * @param aWorker
	 *          �������� Worker ��ü
	 */
	public void addWorker(Worker aWorker)
	{
		synchronized(_waitingWorkers)
		{
			_activeWorkers.addElement(aWorker);
		}
	}

	/**
	 * �����¿� �� Worker�� ���
	 * 
	 * @param aWorker
	 *          �������� Worker ��ü
	 */
	public void addWaitingWorker(Worker aWorker)
	{
		synchronized(_waitingWorkers)
		{
			_waitingWorkers.addElement(aWorker);
		}
	}

	/**
	 * ��ü Worker�� ���� ��ȯ
	 * 
	 * @return ��ü Worker�� ��
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
	 * ���� ī���͸� 1 ����
	 */
	public synchronized void add_currentLoad()
	{
		_currentLoad++;
	}
}