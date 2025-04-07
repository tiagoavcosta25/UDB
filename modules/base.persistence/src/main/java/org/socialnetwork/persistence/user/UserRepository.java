package org.socialnetwork.persistence.user;

import com.couchbase.client.java.query.QueryScanConsistency;
import org.socialnetwork.model.user.User;
import org.springframework.data.couchbase.repository.Collection;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.DynamicProxyable;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.stereotype.Repository;

/**
 * QueryScanConsistency.REQUEST_PLUS ensures that the query waits for the most recent changes to be fully indexed,
 * thus providing a more accurate and up to date view of the data at the cost of additional wait time and processing.
 * <p>
 * User repository
 * The DynamicProxyable interface exposes userRepository.withScope(scope), withCollection() and withOptions() It's
 * necessary on the repository object itself because the withScope() etc. methods need to return an object of type
 * UserRepository so that one can code... userRepository = userRepository.withScope(scopeName) without having
 * to cast the result.
 */
@Repository("userRepository")
@Collection("user")
@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
public interface UserRepository extends CouchbaseRepository<User, String>, DynamicProxyable<UserRepository> {
}
