package array

func Find[T any](items []T, predicate func(T) bool) *T {
	for i := range items {
		if predicate(items[i]) {
			return &items[i]
		}
	}
	return nil
}

func Filter[T any](items []T, predicate func(T) bool) []T {
	var result []T
	for _, item := range items {
		if predicate(item) {
			result = append(result, item)
		}
	}
	return result
}

func Map[T any, R any](items []T, mapper func(T) R) []R {
	result := make([]R, 0, len(items))
	for _, item := range items {
		result = append(result, mapper(item))
	}
	return result
}

func Contains[T comparable](items []T, target T) bool {
	for _, item := range items {
		if item == target {
			return true
		}
	}
	return false
}
