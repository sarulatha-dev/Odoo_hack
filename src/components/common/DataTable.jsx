export default function DataTable({
  columns,
  rows,
  getRowKey,
  emptyMessage = 'No records found.',
  minWidthClassName = 'min-w-[640px]',
  ariaLabel = 'Dashboard data',
}) {
  return (
    <div
      className="overflow-x-auto"
      role="region"
      aria-label={`${ariaLabel} table, scroll horizontally to view all columns`}
      tabIndex={0}
    >
      <table
        className={`w-full ${minWidthClassName} border-collapse text-left text-[15px]`}
        aria-label={ariaLabel}
      >
        <thead>
          <tr className="border-b border-border bg-background/40 text-xs uppercase tracking-[0.12em] text-muted">
            {columns.map((column) => (
              <th
                key={column.key}
                scope="col"
                className={[
                  'px-5 py-3.5 font-semibold first:pl-0 last:pr-0',
                  column.align === 'right' ? 'text-right' : '',
                  column.headerClassName ?? '',
                ].join(' ')}
              >
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {rows.length === 0 ? (
            <tr>
              <td colSpan={columns.length} className="px-4 py-8 text-center text-sm text-muted">
                {emptyMessage}
              </td>
            </tr>
          ) : (
            rows.map((row, index) => (
              <tr
                key={getRowKey ? getRowKey(row) : index}
                className="transition-colors hover:bg-brand/[0.035]"
              >
                {columns.map((column) => (
                  <td
                    key={column.key}
                    className={[
                      'px-5 py-4 align-middle first:pl-0 last:pr-0',
                      column.align === 'right' ? 'text-right' : '',
                      column.className ?? '',
                    ].join(' ')}
                  >
                    {column.render ? column.render(row) : row[column.key]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}
