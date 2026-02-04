package framework.models.response;

/**
 * Response model for loyalty points accrual.
 */
public class LoyaltyAccrueResponse {

    private String memberId;
    private int pointsAccrued;
    private int newBalance;
    private String correlationId;

    public LoyaltyAccrueResponse() {
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public int getPointsAccrued() {
        return pointsAccrued;
    }

    public void setPointsAccrued(int pointsAccrued) {
        this.pointsAccrued = pointsAccrued;
    }

    public int getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(int newBalance) {
        this.newBalance = newBalance;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
