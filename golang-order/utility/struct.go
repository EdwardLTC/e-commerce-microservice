package utility

import (
	"encoding/json"
)

// StructToMap converts any struct into map[string]any safely.
// It returns an empty map if conversion fails.
func StructToMap(v any) map[string]any {
	bytes, err := json.Marshal(v)
	if err != nil {
		return map[string]any{"_error": err.Error()}
	}

	var result map[string]any
	if err := json.Unmarshal(bytes, &result); err != nil {
		return map[string]any{"_error": err.Error()}
	}

	return result
}
