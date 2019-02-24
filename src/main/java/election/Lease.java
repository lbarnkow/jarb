package election;

public class Lease {
	private String leaderId;
	private long leaseAcquired;
	private long leaseExpiration;

	Lease() {
		this.leaseAcquired = System.currentTimeMillis();
		refreshExpiration();
	}

	public String getLeaderId() {
		return leaderId;
	}

	public void setLeaderId(String leaderId) {
		this.leaderId = leaderId;
	}

	public long getLeaseAcquired() {
		return leaseAcquired;
	}

	public void setLeaseAcquired(long leaseAcquired) {
		this.leaseAcquired = leaseAcquired;
	}

	public long getLeaseExpiration() {
		return leaseExpiration;
	}

	public void setLeaseExpiration(long leaseExpiration) {
		this.leaseExpiration = leaseExpiration;
	}

	public boolean isExpired() {
		long now = System.currentTimeMillis();
		return leaseExpiration < now;
	}

	public boolean isLeader(String supposedLeaderId) {
		return supposedLeaderId.equals(leaderId);
	}

	public void refreshExpiration() {
		this.leaseExpiration = System.currentTimeMillis() + Config.LEASE_TIME_TO_LIVE_MSEC;
	}
}