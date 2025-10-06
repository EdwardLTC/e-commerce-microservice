export const stringValue = (value: string | number | boolean): { value: string } => {
  return { value: String(value ?? '') };
};

export const boolValue = (value: string | number | boolean): { value: boolean } => {
  return { value: Boolean(value ?? false) };
};

export const bytesValue = (value: string | number): { value: Buffer } => {
  return { value: Buffer.from(String(value ?? ''), 'utf-8') };
};

export const numberValue = (value: string | number): { value: number } => {
  return { value: Number(value ?? 0) };
};
