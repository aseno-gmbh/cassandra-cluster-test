package de.aseno.spikes;


import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.retry.RetryDecision;
import com.datastax.oss.driver.api.core.retry.RetryVerdict;
import com.datastax.oss.driver.api.core.servererrors.CoordinatorException;
import com.datastax.oss.driver.api.core.servererrors.WriteType;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.internal.core.retry.ConsistencyDowngradingRetryVerdict;
import com.datastax.oss.driver.internal.core.retry.DefaultRetryPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.NonNull;

public class ConsistencyRetryPolicy extends DefaultRetryPolicy {

	private static final Logger LOG = LoggerFactory.getLogger(ConsistencyRetryPolicy.class);
	private ConsistencyLevel consistencyLevel;

	private ConsistencyLevel lowerConsistencyLevel;

	private int numOfRetries;

	public ConsistencyRetryPolicy(DriverContext context, String profileName) {
		super(context, profileName);
	}

	@Override
	public RetryDecision onReadTimeout(Request request, ConsistencyLevel cl, int blockFor, int received,
			boolean dataPresent, int retryCount) {
		LOG.info("onReadTimeout");
		RetryDecision decision = (retryCount < numOfRetries && received >= blockFor && !dataPresent)
				? RetryDecision.RETRY_SAME
						: RetryDecision.RETHROW;

		if (decision == RetryDecision.RETRY_SAME) {
			LOG.info(RETRYING_ON_READ_TIMEOUT, cl, blockFor, received, false, retryCount);
		}

		return decision;
	}

	@Override
	public RetryDecision onWriteTimeout(Request request, ConsistencyLevel cl, WriteType writeType, int blockFor,
			int received, int retryCount) {
		LOG.info("onWriteTimeout");
		return null;
	}

	@Override
	public RetryDecision onUnavailable(Request request, ConsistencyLevel cl, int required, int alive, int retryCount) {
		LOG.info("onUnavailable");
		return null;
	}

	@Override
	public RetryDecision onRequestAborted(Request request, Throwable error, int retryCount) {
		LOG.info("onRequestAborted");
		return null;
	}

	@Override
	public RetryDecision onErrorResponse(Request request, CoordinatorException error, int retryCount) {
		LOG.info("onErrorResponse");
		return null;
	}

	@Override
	public void close() {
		LOG.info("close");

	}


	@Override
	public RetryVerdict onReadTimeoutVerdict(@NonNull Request request, @NonNull ConsistencyLevel cl, int blockFor, int received, boolean dataPresent, int retryCount) {
		RetryVerdict verdict;
		LOG.info("--> on onReadTimeoutVerdict");
		if (retryCount != 0) {
			verdict = RetryVerdict.RETHROW;
		} else if (cl.isSerial()) {
			verdict = RetryVerdict.RETHROW;
		} else if (received < blockFor) {
			verdict = this.maybeDowngrade(cl, received, retryCount);
		} else if (!dataPresent) {
			verdict = RetryVerdict.RETRY_SAME;
		} else {
			verdict = RetryVerdict.RETHROW;
		}

		return verdict;
	}

	@Override
	public RetryVerdict onUnavailableVerdict(@NonNull Request request, @NonNull ConsistencyLevel cl, int required, int alive, int retryCount) {

		LOG.info(String.format("onUnavailableVerdict Alive:%d Retrycount:%d + CL: " + cl.name(), alive, retryCount));
		LOG.info("current request: " +request.getCustomPayload().toString());
		if (retryCount < 3 && alive > 1) {
			//            LOG.warn(String.format("Node not available -> Try next. Alive:%d Retrycount:%d", alive, retryCount));
			//            return RetryVerdict.RETRY_NEXT;
			//        } else if (retryCount == 0) {

			LOG.warn(String.format("Only one node alive -> Downgraded to LOCAL_QUORUM.  Alive:%d Retrycount:%d", alive, retryCount));
			ConsistencyDowngradingRetryVerdict rv = new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.LOCAL_QUORUM);
			
			LOG.info("retry decision " + rv.toString());
			return rv;
		}
		if (retryCount > 10) {
			LOG.error("I've reached retryCount > 10. So I am ignoring this statement");
			return RetryVerdict.IGNORE;
		}

		return maybeDowngrade(cl, alive, retryCount);
	}

	@NonNull
	private RetryVerdict maybeDowngrade(@NonNull ConsistencyLevel cl, int alive, int retryCount) {
		LOG.info("maybeDowngrade with CL: -> " + cl.name());
		if(cl == ConsistencyLevel.ALL) {
			LOG.warn(String.format("ALL could not be reached -> Downgraded to LOCAL_QUORUM. Alive:%d Retrycount:%d", alive, retryCount));
			return new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.QUORUM);
		} else if(cl == ConsistencyLevel.QUORUM) {
			LOG.warn(String.format("QUORUM could not be reached -> Downgraded to LOCAL_QUORUM. Alive:%d Retrycount:%d", alive, retryCount));
			return new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.LOCAL_QUORUM);
		} else if(cl == ConsistencyLevel.EACH_QUORUM) {
			LOG.warn(String.format("EACH_QUORUM could not be reached -> Downgraded to LOCAL_QUORUM. Alive:%d Retrycount:%d", alive, retryCount));
			return new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.LOCAL_QUORUM);
		}else if(cl == ConsistencyLevel.LOCAL_QUORUM) {
			LOG.error(String.format("LOCAL_QUORUM could not be reached -> Downgraded to LOCAL_ONE. Alive:%d Retrycount:%d", alive, retryCount));
			return new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.LOCAL_ONE);
		}else if(cl == ConsistencyLevel.SERIAL) {
			ConsistencyDowngradingRetryVerdict rv = new ConsistencyDowngradingRetryVerdict(ConsistencyLevel.LOCAL_QUORUM);
			LOG.info("retry decision " + rv.toString());
			LOG.error(String.format("LOCAL_QUORUM could not be reached -> Downgraded to LOCAL_ONE. Alive:%d Retrycount:%d", alive, retryCount));
			return rv;
		}else {
			LOG.error(String.format("All retries failed -> Throw error. Alive:%d Retrycount:%d", alive, retryCount));
			return RetryVerdict.RETHROW;
		}
	}

}
